package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.backend.instancing.ratelimit.NonLimiter;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.light.LightUpdater;
import com.mojang.math.Vector3f;

import net.minecraft.client.Camera;
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
		int incr = 500;
		int size = instances.size();
		int start = 0;
		while (start < size) {
			int end = Math.min(start + incr, size);

			var sub = instances.subList(start, end);
			taskEngine.submit(() -> {
				for (TickableInstance instance : sub) {
					tickInstance(cX, cY, cZ, instance);
				}
			});

			start += incr;
		}
	}

	protected void tickInstance(int cX, int cY, int cZ, TickableInstance instance) {
		if (!instance.decreaseTickRateWithDistance()) {
			instance.tick();
			return;
		}

		BlockPos pos = instance.getWorldPosition();

		int dX = pos.getX() - cX;
		int dY = pos.getY() - cY;
		int dZ = pos.getZ() - cZ;

		if (tick.shouldUpdate(dX, dY, dZ)) instance.tick();
	}

	public void beginFrame(TaskEngine taskEngine, Camera camera) {
		frame.tick();
		processQueuedAdditions();

		Vector3f look = camera.getLookVector();
		float lookX = look.x();
		float lookY = look.y();
		float lookZ = look.z();

		// integer camera pos
		int cX = (int) camera.getPosition().x;
		int cY = (int) camera.getPosition().y;
		int cZ = (int) camera.getPosition().z;

		var instances = getStorage().getInstancesForUpdate();
		int incr = 500;
		int size = instances.size();
		int start = 0;
		while (start < size) {
			int end = Math.min(start + incr, size);

			var sub = instances.subList(start, end);
			taskEngine.submit(() -> {
				for (DynamicInstance dyn : sub) {
					updateInstance(dyn, lookX, lookY, lookZ, cX, cY, cZ);
				}
			});

			start += incr;
		}
	}

	protected void updateInstance(DynamicInstance dyn, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		if (!dyn.decreaseFramerateWithDistance()) {
			dyn.beginFrame();
			return;
		}

		BlockPos worldPos = dyn.getWorldPosition();
		int dX = worldPos.getX() - cX;
		int dY = worldPos.getY() - cY;
		int dZ = worldPos.getZ() - cZ;

		// is it more than 2 blocks behind the camera?
		int dist = 2;
		float dot = (dX + lookX * dist) * lookX + (dY + lookY * dist) * lookY + (dZ + lookZ * dist) * lookZ;
		if (dot < 0) {
			return;
		}

		if (frame.shouldUpdate(dX, dY, dZ))
			dyn.beginFrame();
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

		if (canCreateInstance(obj)) {
			getStorage().remove(obj);
		}
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
