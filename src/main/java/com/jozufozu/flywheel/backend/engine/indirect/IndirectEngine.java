package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.List;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;

public class IndirectEngine extends AbstractEngine {
	private final IndirectPrograms programs;
	private final IndirectDrawManager drawManager;
	private final Flag flushFlag = new NamedFlag("flushed");

	public IndirectEngine(IndirectPrograms programs, int maxOriginDistance) {
		super(maxOriginDistance);
		programs.acquire();
		this.programs = programs;
		drawManager = new IndirectDrawManager(this.programs);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SyncedPlan.of(this::flushDrawManager);
	}

	private void flushDrawManager(RenderContext ctx) {
		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.updateContext(ctx);
			drawManager.flush();
		}
		flushFlag.raise();
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		executor.syncUntil(flushFlag::isRaised);
		if (stage.isLast()) {
			flushFlag.lower();
		}

		if (!drawManager.hasStage(stage)) {
			return;
		}

		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.renderStage(stage);
		}
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		executor.syncUntil(flushFlag::isRaised);

		drawManager.renderCrumbling(crumblingBlocks);
	}

	@Override
	protected InstancerStorage<? extends AbstractInstancer<?>> getStorage() {
		return drawManager;
	}

	@Override
	public void delete() {
		drawManager.delete();
		programs.release();
	}
}
