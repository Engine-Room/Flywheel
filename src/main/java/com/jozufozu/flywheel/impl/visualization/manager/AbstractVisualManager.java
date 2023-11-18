package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visual.VisualTickContext;
import com.jozufozu.flywheel.api.visualization.VisualManager;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.visualization.FrameContext;
import com.jozufozu.flywheel.impl.visualization.TickContext;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiterImpl;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;
import com.jozufozu.flywheel.lib.task.SimplePlan;

public abstract class AbstractVisualManager<T> implements VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected DistanceUpdateLimiterImpl tickLimiter;
	protected DistanceUpdateLimiterImpl frameLimiter;

	public AbstractVisualManager() {
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

	@Override
	public int getVisualCount() {
		return getStorage().getAllVisuals().size();
	}

	@Override
	public void queueAdd(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.add(obj));
	}

	@Override
	public void queueRemove(T obj) {
		queue.add(Transaction.remove(obj));
	}

	@Override
	public void queueUpdate(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.update(obj));
	}

	public Plan<Float> createRecreationPlan() {
		return SimplePlan.of(getStorage()::recreateAll);
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	protected void processQueue(float partialTick) {
		var storage = getStorage();
		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			transaction.apply(storage, partialTick);
		}
	}

	public Plan<TickContext> createTickPlan() {
		return SimplePlan.<TickContext>of(() -> {
					tickLimiter.tick();
					processQueue(0);
				})
				.thenMap(this::createVisualTickContext, getStorage().getTickPlan());
	}

	public Plan<FrameContext> createFramePlan() {
		return SimplePlan.<FrameContext>of(context -> {
					frameLimiter.tick();
					processQueue(context.partialTick());
				})
				.thenMap(this::createVisualContext, getStorage().getFramePlan());
	}

	private VisualFrameContext createVisualContext(FrameContext ctx) {
		return new VisualFrameContext(ctx.cameraX(), ctx.cameraY(), ctx.cameraZ(), ctx.frustum(), ctx.partialTick(), frameLimiter);
	}

	private VisualTickContext createVisualTickContext(TickContext ctx) {
		return new VisualTickContext(ctx.cameraX(), ctx.cameraY(), ctx.cameraZ(), frameLimiter);
	}
}
