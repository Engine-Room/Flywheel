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
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

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
			GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
			int prevActiveTexture = GlStateManager._getActiveTexture();
			gameRenderer.overlayTexture().setupOverlayColor();
			gameRenderer.lightTexture().turnOnLightLayer();

			GlTextureUnit.T1.makeActive();
			RenderSystem.bindTexture(RenderSystem.getShaderTexture(1));
			GlTextureUnit.T2.makeActive();
			RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));

			drawManager.renderStage(stage);

			MaterialRenderState.reset();

			gameRenderer.overlayTexture().teardownOverlayColor();
			gameRenderer.lightTexture().turnOffLightLayer();
			GlStateManager._activeTexture(prevActiveTexture);
		}
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		executor.syncUntil(flushFlag::isRaised);

		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.renderCrumbling(crumblingBlocks);

			MaterialRenderState.reset();
		}
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
