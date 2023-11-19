package com.jozufozu.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Flag;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.RaisePlan;
import com.jozufozu.flywheel.lib.task.SyncedPlan;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class IndirectEngine extends AbstractEngine {
	private final IndirectDrawManager drawManager = new IndirectDrawManager();
	private final Flag flushFlag = new NamedFlag("flushed");

	public IndirectEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(type, model, stage);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SyncedPlan.<RenderContext>of(this::flushDrawManager)
				.then(RaisePlan.raise(flushFlag));
	}

	private void flushDrawManager() {
		try (var state = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		if (drawManager.hasStage(stage)) {
			executor.syncTo(flushFlag);

			try (var restoreState = GlStateTracker.getRestoreState()) {
				setup();

				for (var list : drawManager.renderLists.values()) {
					list.submit(stage);
				}
			}
		}

		if (stage.isLast()) {
			// Need to sync here to ensure this frame has everything executed
			// in case we didn't have any stages to draw this frame.
			executor.syncTo(flushFlag);
			executor.lower(flushFlag);
		}
	}

	private void setup() {
		GlTextureUnit.T2.makeActive();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL32.GL_LEQUAL);
		RenderSystem.enableCull();
	}

	@Override
	protected void onRenderOriginChanged() {
		drawManager.clearInstancers();
	}

	@Override
	public void delete() {
		drawManager.invalidate();
	}
}
