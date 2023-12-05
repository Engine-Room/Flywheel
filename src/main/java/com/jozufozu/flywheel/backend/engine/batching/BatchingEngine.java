package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.lib.task.DynamicNestedPlan;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.MapContextPlan;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SimplePlan;

public class BatchingEngine extends AbstractEngine {
	private final BatchedDrawManager drawManager = new BatchedDrawManager();

	private final Flag flushFlag = new NamedFlag("flushed");

	public BatchingEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SimplePlan.<RenderContext>of(() -> {
			drawManager.flush();
			flushFlag.raise();
		}).then(MapContextPlan.map((RenderContext ctx) -> BatchContext.create(ctx, renderOrigin))
				.to(DynamicNestedPlan.of(drawManager::getStagePlans)));
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		executor.syncUntil(flushFlag::isRaised);
		if (stage.isLast()) {
			flushFlag.lower();
		}

		drawManager.renderStage(executor, stage);
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		executor.syncUntil(flushFlag::isRaised);

		var batchContext = BatchContext.create(context, this.renderOrigin);
		BatchedCrumbling.render(crumblingBlocks, batchContext, this.drawManager.drawTracker);
	}

	@Override
	protected InstancerStorage<? extends AbstractInstancer<?>> getStorage() {
		return drawManager;
	}

	@Override
	public void delete() {
		drawManager.delete();
	}
}
