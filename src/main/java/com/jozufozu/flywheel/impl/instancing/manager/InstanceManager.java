package com.jozufozu.flywheel.impl.instancing.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.instancing.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.instancing.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.impl.instancing.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.instancing.storage.Storage;

public abstract class InstanceManager<T> {
	private final Set<T> queuedAdditions = new HashSet<>(64);
	private final Set<T> queuedUpdates = new HashSet<>(64);

	protected DistanceUpdateLimiter tickLimiter;
	protected DistanceUpdateLimiter frameLimiter;

	public InstanceManager() {
		tickLimiter = createUpdateLimiter();
		frameLimiter = createUpdateLimiter();
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
		return getStorage().getAllInstances().size();
	}

	public void add(T obj) {
		if (!canCreateInstance(obj)) {
			return;
		}

		getStorage().add(obj);
	}

	public void queueAdd(T obj) {
		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedAdditions) {
			queuedAdditions.add(obj);
		}
	}

	/**
	 * Update the instance associated with an object.
	 *
	 * <p>
	 *     By default this is the only hook an {@link Instance} has to change its internal state. This is the lowest frequency
	 *     update hook {@link Instance} gets. For more frequent updates, see {@link TickableInstance} and
	 *     {@link DynamicInstance}.
	 * </p>
	 *
	 * @param obj the object to update.
	 */
	public void update(T obj) {
		if (!canCreateInstance(obj)) {
			return;
		}

		getStorage().update(obj);
	}

	public void queueUpdate(T obj) {
		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedUpdates) {
			queuedUpdates.add(obj);
		}
	}

	public void remove(T obj) {
		getStorage().remove(obj);
	}

	public void recreateAll() {
		getStorage().recreateAll();
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	protected void processQueuedAdditions() {
		if (queuedAdditions.isEmpty()) {
			return;
		}

		List<T> queued;

		synchronized (queuedAdditions) {
			queued = List.copyOf(queuedAdditions);
			queuedAdditions.clear();
		}

		if (!queued.isEmpty()) {
			queued.forEach(getStorage()::add);
		}
	}

	protected void processQueuedUpdates() {
		if (queuedUpdates.isEmpty()) {
			return;
		}

		List<T> queued;

		synchronized (queuedUpdates) {
			queued = List.copyOf(queuedUpdates);
			queuedUpdates.clear();
		}

		if (!queued.isEmpty()) {
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
		processQueuedAdditions();
		processQueuedUpdates();

		var instances = getStorage().getTickableInstances();
		distributeWork(executor, instances, instance -> tickInstance(instance, cameraX, cameraY, cameraZ));
	}

	protected void tickInstance(TickableInstance instance, double cameraX, double cameraY, double cameraZ) {
		if (!instance.decreaseTickRateWithDistance() || tickLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			instance.tick();
		}
	}

	public void beginFrame(TaskExecutor executor, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		frameLimiter.tick();
		processQueuedAdditions();
		processQueuedUpdates();

		var instances = getStorage().getDynamicInstances();
		distributeWork(executor, instances, instance -> updateInstance(instance, cameraX, cameraY, cameraZ, frustum));
	}

	protected void updateInstance(DynamicInstance instance, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		if (!instance.decreaseFramerateWithDistance() || frameLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			if (instance.isVisible(frustum)) {
				instance.beginFrame();
			}
		}
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
}
