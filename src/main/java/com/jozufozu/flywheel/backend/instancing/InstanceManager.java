package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.NonLimiter;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.joml.FrustumIntersection;

import net.minecraft.core.BlockPos;

public abstract class InstanceManager<T> {

	private final Set<T> queuedAdditions;
	private final Set<T> queuedUpdates;

	protected DistanceUpdateLimiter frame;
	protected DistanceUpdateLimiter tick;

	public InstanceManager() {
		this.queuedUpdates = new HashSet<>(64);
		this.queuedAdditions = new HashSet<>(64);

		frame = createUpdateLimiter();
		tick = createUpdateLimiter();
	}

	public abstract Storage<T> getStorage();

	/**
	 * Is the given object currently capable of being instanced?
	 *
	 * <p>
	 *     This won't be the case for TEs or entities that are outside of loaded chunks.
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
	public int getObjectCount() {
		return getStorage().getObjectCount();
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
	public void tick(TaskEngine taskEngine, double cameraX, double cameraY, double cameraZ) {
		tick.tick();
		processQueuedUpdates();

		// integer camera pos as a micro-optimization
		int cX = (int) cameraX;
		int cY = (int) cameraY;
		int cZ = (int) cameraZ;

		var instances = getStorage().getInstancesForTicking();
		distributeWork(taskEngine, instances, instance -> tickInstance(instance, cX, cY, cZ));
	}

	protected void tickInstance(TickableInstance instance, int cX, int cY, int cZ) {
		if (!instance.decreaseTickRateWithDistance()) {
			instance.tick();
			return;
		}

		BlockPos pos = instance.getWorldPosition();

		int dX = pos.getX() - cX;
		int dY = pos.getY() - cY;
		int dZ = pos.getZ() - cZ;

		if (!tick.shouldUpdate(dX, dY, dZ)) {
			return;
		}

		instance.tick();
	}

	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		frame.tick();
		processQueuedAdditions();

		// integer camera pos
		BlockPos cameraIntPos = context.camera().getBlockPosition();
		int cX = cameraIntPos.getX();
		int cY = cameraIntPos.getY();
		int cZ = cameraIntPos.getZ();
		FrustumIntersection culler = context.culler();

		var instances = getStorage().getInstancesForUpdate();
		distributeWork(taskEngine, instances, instance -> updateInstance(instance, culler, cX, cY, cZ));
	}

	private static <I> void distributeWork(TaskEngine taskEngine, List<I> instances, Consumer<I> action) {
		final int size = instances.size();
		final int threadCount = taskEngine.getThreadCount();

		if (threadCount == 1) {
			taskEngine.submit(() -> instances.forEach(action));
		} else {
			final int stride = Math.max(size / (threadCount * 2), 1);
			for (int start = 0; start < size; start += stride) {
				int end = Math.min(start + stride, size);

				var sub = instances.subList(start, end);
				taskEngine.submit(() -> sub.forEach(action));
			}
		}
	}

	protected void updateInstance(DynamicInstance dyn, FrustumIntersection test, int cX, int cY, int cZ) {
		if (!dyn.decreaseFramerateWithDistance()) {
			dyn.beginFrame();
			return;
		}

		BlockPos worldPos = dyn.getWorldPosition();
		int dX = worldPos.getX() - cX;
		int dY = worldPos.getY() - cY;
		int dZ = worldPos.getZ() - cZ;

		if (!frame.shouldUpdate(dX, dY, dZ)) {
			return;
		}

		if (dyn.checkFrustum(test)) {
			dyn.beginFrame();
		}

	}

	public void add(T obj) {
		if (!Backend.isOn()) return;

		if (canCreateInstance(obj)) {
			getStorage().add(obj);
		}
	}

	public void queueAdd(T obj) {
		if (!Backend.isOn()) {
			return;
		}

		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedAdditions) {
			queuedAdditions.add(obj);
		}
	}

	public void queueUpdate(T obj) {
		if (!Backend.isOn()) return;

		if (!canCreateInstance(obj)) {
			return;
		}

		synchronized (queuedUpdates) {
			queuedUpdates.add(obj);
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
		if (!Backend.isOn()) return;

		if (canCreateInstance(obj)) {
			getStorage().update(obj);
		}
	}

	public void remove(T obj) {
		if (!Backend.isOn()) return;

		getStorage().remove(obj);
	}

	public void invalidate() {
		getStorage().invalidate();
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

	public void onOriginShift() {
		getStorage().recreateAll();
	}

	public void delete() {
		for (AbstractInstance value : getStorage().allInstances()) {
			LightUpdater.get(value.level).removeListener(value);
		}
	}
}
