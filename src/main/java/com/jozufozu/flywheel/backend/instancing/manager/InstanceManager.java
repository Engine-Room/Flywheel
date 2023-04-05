package com.jozufozu.flywheel.backend.instancing.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.instancing.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.NonLimiter;
import com.jozufozu.flywheel.backend.instancing.storage.Storage;
import com.jozufozu.flywheel.config.FlwConfig;

public abstract class InstanceManager<T> {
	private final Set<T> queuedAdditions = new HashSet<>(64);
	private final Set<T> queuedUpdates = new HashSet<>(64);

	protected DistanceUpdateLimiter frameLimiter;
	protected DistanceUpdateLimiter tickLimiter;

	public InstanceManager() {
		frameLimiter = createUpdateLimiter();
		tickLimiter = createUpdateLimiter();
	}

	protected abstract Storage<T> getStorage();

	/**
	 * Is the given object currently capable of being instanced?
	 *
	 * <p>
	 *     This won't be the case for block entities or entities that are outside of loaded chunks.
	 * </p>
	 *
	 * @return true if the object is currently capable of being instanced.
	 */
	protected abstract boolean canCreateInstance(T obj);

	protected DistanceUpdateLimiter createUpdateLimiter() {
		if (FlwConfig.get().limitUpdates()) {
			return new BandedPrimeLimiter();
		} else {
			return new NonLimiter();
		}
	}

	/**
	 * Get the number of game objects that are currently being instanced.
	 *
	 * @return The object count.
	 */
	public int getInstanceCount() {
		return getStorage().getInstanceCount();
	}

	public void add(T obj) {
		if (!BackendManager.isOn()) return;

		if (canCreateInstance(obj)) {
			getStorage().add(obj);
		}
	}

	public void queueAdd(T obj) {
		if (!BackendManager.isOn()) {
			return;
		}

		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedAdditions) {
			queuedAdditions.add(obj);
		}
	}

	public void queueAddAll(Collection<? extends T> objects) {
		if (!BackendManager.isOn() || objects.isEmpty()) {
			return;
		}

		synchronized (queuedAdditions) {
			queuedAdditions.addAll(objects);
		}
	}

	/**
	 * Update the instance associated with an object.
	 *
	 * <p>
	 *     By default this is the only hook an IInstance has to change its internal state. This is the lowest frequency
	 *     update hook IInstance gets. For more frequent updates, see {@link TickableInstance} and
	 *     {@link DynamicInstance}.
	 * </p>
	 *
	 * @param obj the object to update.
	 */
	public void update(T obj) {
		if (!BackendManager.isOn()) return;

		if (canCreateInstance(obj)) {
			getStorage().update(obj);
		}
	}

	public void queueUpdate(T obj) {
		if (!BackendManager.isOn()) return;

		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedUpdates) {
			queuedUpdates.add(obj);
		}
	}

	public void remove(T obj) {
		if (!BackendManager.isOn()) return;

		getStorage().remove(obj);
	}

	public void onOriginShift() {
		getStorage().recreateAll();
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	public void delete() {
		for (Instance instance : getStorage().getAllInstances()) {
			instance.delete();
		}
	}

	protected void processQueuedAdditions() {
		if (queuedAdditions.isEmpty()) {
			return;
		}

		ArrayList<T> queued;

		synchronized (queuedAdditions) {
			queued = new ArrayList<>(queuedAdditions);
			queuedAdditions.clear();
		}

		if (!queued.isEmpty()) {
			queued.forEach(getStorage()::add);
		}
	}

	protected void processQueuedUpdates() {
		ArrayList<T> queued;

		synchronized (queuedUpdates) {
			queued = new ArrayList<>(queuedUpdates);
			queuedUpdates.clear();
		}

		if (queued.size() > 0) {
			queued.forEach(getStorage()::update);
		}
	}

	/**
	 * Ticks the InstanceManager.
	 *
	 * <p>
	 *     {@link TickableInstance}s get ticked.
	 *     <br>
	 *     Queued updates are processed.
	 * </p>
	 */
	public void tick(TaskExecutor executor, double cameraX, double cameraY, double cameraZ) {
		tickLimiter.tick();
		processQueuedUpdates();

		var instances = getStorage().getInstancesForTicking();
		distributeWork(executor, instances, instance -> tickInstance(instance, cameraX, cameraY, cameraZ));
	}

	protected void tickInstance(TickableInstance instance, double cX, double cY, double cZ) {
		if (!instance.decreaseTickRateWithDistance()) {
			instance.tick();
			return;
		}

		var dsq = instance.distanceSquared(cX, cY, cZ);

		if (!tickLimiter.shouldUpdate(dsq)) {
			return;
		}

		instance.tick();
	}

	public void beginFrame(TaskExecutor executor, RenderContext context) {
		frameLimiter.tick();
		processQueuedAdditions();

		var cameraPos = context.camera()
				.getPosition();
		double cX = cameraPos.x;
		double cY = cameraPos.y;
		double cZ = cameraPos.z;
		FrustumIntersection culler = context.culler();

		var instances = getStorage().getInstancesForUpdate();
		distributeWork(executor, instances, instance -> updateInstance(instance, culler, cX, cY, cZ));
	}

	private static <I> void distributeWork(TaskExecutor executor, List<I> instances, Consumer<I> action) {
		final int size = instances.size();
		final int threadCount = executor.getThreadCount();

		if (threadCount == 1) {
			executor.execute(() -> instances.forEach(action));
		} else {
			final int stride = Math.max(size / (threadCount * 2), 1);
			for (int start = 0; start < size; start += stride) {
				int end = Math.min(start + stride, size);

				var sub = instances.subList(start, end);
				executor.execute(() -> sub.forEach(action));
			}
		}
	}

	protected void updateInstance(DynamicInstance instance, FrustumIntersection frustum, double cX, double cY, double cZ) {
		if (!instance.decreaseFramerateWithDistance()) {
			instance.beginFrame();
			return;
		}

		if (!frameLimiter.shouldUpdate(instance.distanceSquared(cX, cY, cZ))) {
			return;
		}

		if (instance.checkFrustum(frustum)) {
			instance.beginFrame();
		}
	}
}
