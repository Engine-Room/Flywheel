package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.visualization.FrameContext;
import com.jozufozu.flywheel.impl.visualization.TickContext;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiterImpl;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;
import com.jozufozu.flywheel.lib.task.SimplePlan;
import com.jozufozu.flywheel.util.Unit;

public abstract class VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected DistanceUpdateLimiterImpl tickLimiter;
	protected DistanceUpdateLimiterImpl frameLimiter;

	public VisualManager() {
		tickLimiter = createUpdateLimiter();
		frameLimiter = createUpdateLimiter();
	}

	protected abstract Storage<T> getStorage();

	protected DistanceUpdateLimiterImpl createUpdateLimiter() {
		if (FlwConfig.get()
				.limitUpdates()) {
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
				.thenMap(this::createVisualTickContext, getStorage().getTickPlan());
	}

	public Plan<FrameContext> createFramePlan() {
		return SimplePlan.<FrameContext>of(() -> {
					frameLimiter.tick();
					processQueue();
				})
				.thenMap(this::createVisualContext, getStorage().getFramePlan());
	}

	private VisualFrameContext createVisualContext(FrameContext ctx) {
		return new VisualFrameContext(ctx.cameraX(), ctx.cameraY(), ctx.cameraZ(), ctx.frustum(), frameLimiter);
	}

	private VisualTickContext createVisualTickContext(TickContext ctx) {
		return new VisualTickContext(ctx.cameraX(), ctx.cameraY(), ctx.cameraZ(), frameLimiter);
	}
}
