package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;
import com.jozufozu.flywheel.lib.task.RunOnAllPlan;
import com.jozufozu.flywheel.lib.task.SimplePlan;

public abstract class VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected DistanceUpdateLimiter tickLimiter;
	protected DistanceUpdateLimiter frameLimiter;

	public VisualManager() {
		tickLimiter = createUpdateLimiter();
		frameLimiter = createUpdateLimiter();
	}

	protected abstract Storage<T> getStorage();

	protected DistanceUpdateLimiter createUpdateLimiter() {
		if (FlwConfig.get().limitUpdates()) {
			return new BandedPrimeLimiter();
		} else {
			return new NonLimiter();
		}
	}

	/**
	 * Get the number of game objects that are currently being visualized.
	 *
	 * @return The object count.
	 */
	public int getVisualCount() {
		return getStorage().getAllVisuals().size();
	}

	public void queueAdd(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.add(obj));
	}

	public void queueRemove(T obj) {
		queue.add(Transaction.remove(obj));
	}

	public void queueUpdate(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.update(obj));
	}

	public void recreateAll() {
		getStorage().recreateAll();
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	protected void processQueue() {
		var storage = getStorage();
		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			transaction.apply(storage);
		}
	}

	public Plan planThisTick(double cameraX, double cameraY, double cameraZ) {
		return SimplePlan.of(() -> {
					tickLimiter.tick();
					processQueue();
				})
				.then(RunOnAllPlan.of(getStorage()::getTickableVisuals, instance -> tickInstance(instance, cameraX, cameraY, cameraZ)));
	}

	protected void tickInstance(TickableVisual instance, double cameraX, double cameraY, double cameraZ) {
		if (!instance.decreaseTickRateWithDistance() || tickLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			instance.tick();
		}
	}

	public Plan planThisFrame(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		return SimplePlan.of(() -> {
					frameLimiter.tick();
					processQueue();
				})
				.then(RunOnAllPlan.of(getStorage()::getDynamicVisuals, instance -> updateInstance(instance, cameraX, cameraY, cameraZ, frustum)));
	}

	protected void updateInstance(DynamicVisual instance, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		if (!instance.decreaseFramerateWithDistance() || frameLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			if (instance.isVisible(frustum)) {
				instance.beginFrame();
			}
		}
	}
}
