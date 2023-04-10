package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class IndirectEngine extends AbstractEngine {
	private final IndirectDrawManager drawManager = new IndirectDrawManager();

	public IndirectEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(type, model, stage);
	}

	@Override
	public void beginFrame(TaskExecutor executor, RenderContext context) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			setup();

			for (var list : drawManager.renderLists.values()) {
				list.submit(stage);
			}
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
		drawManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL46 Indirect");
		info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
	}
}
