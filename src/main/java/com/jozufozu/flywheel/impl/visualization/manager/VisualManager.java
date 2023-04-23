package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.TickContext;
import com.jozufozu.flywheel.impl.visualization.FrameContext;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;
import com.jozufozu.flywheel.lib.task.RunOnAllWithContextPlan;
import com.jozufozu.flywheel.lib.task.SimplePlan;
import com.jozufozu.flywheel.util.Unit;

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

	public Plan<Unit> createRecreationPlan() {
		// TODO: parallelize recreation?
		return SimplePlan.of(getStorage()::recreateAll);
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

	public Plan<TickContext> createTickPlan() {
		return SimplePlan.<TickContext>of(() -> {
					tickLimiter.tick();
					processQueue();
				})
				.then(RunOnAllWithContextPlan.of(getStorage()::getTickableVisuals, this::tickInstance));
	}

	protected void tickInstance(TickableVisual instance, TickContext c) {
		if (!instance.decreaseTickRateWithDistance() || tickLimiter.shouldUpdate(instance.distanceSquared(c.cameraX(), c.cameraY(), c.cameraZ()))) {
			instance.tick();
		}
	}

	public Plan<FrameContext> createFramePlan() {
		return SimplePlan.<FrameContext>of(() -> {
					frameLimiter.tick();
					processQueue();
				})
				.then(RunOnAllWithContextPlan.of(getStorage()::getDynamicVisuals, this::updateInstance));
	}

	protected void updateInstance(DynamicVisual instance, FrameContext c) {
		if (!instance.decreaseFramerateWithDistance() || frameLimiter.shouldUpdate(instance.distanceSquared(c.cameraX(), c.cameraY(), c.cameraZ()))) {
			if (instance.isVisible(c.frustum())) {
				instance.beginFrame();
			}
		}
	}
}
