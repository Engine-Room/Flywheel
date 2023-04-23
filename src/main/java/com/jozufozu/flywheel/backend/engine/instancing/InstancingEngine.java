package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.lib.task.OnMainThreadPlan;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class InstancingEngine extends AbstractEngine {
	private final Context context;
	private final InstancingDrawManager drawManager = new InstancingDrawManager();

	public InstancingEngine(int maxOriginDistance, Context context) {
		super(maxOriginDistance);
		this.context = context;
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(type, model, stage);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return OnMainThreadPlan.of(this::flushDrawManager);
	}

	private void flushDrawManager() {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		executor.syncPoint();

		try (var state = GlStateTracker.getRestoreState()) {
			setup();

			render(drawSet);
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

	private void render(InstancingDrawManager.DrawSet drawSet) {
		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::isInvalid);

			if (drawCalls.isEmpty()) {
				continue;
			}

			setup(shader);

			shader.material().setup();

			for (var drawCall : drawCalls) {
				drawCall.render();
			}

			shader.material().clear();
		}
	}

	private void setup(ShaderState desc) {
		var material = desc.material();
		var vertexType = desc.vertexType();
		var instanceType = desc.instanceType();

		var program = InstancingPrograms.get()
				.get(vertexType, instanceType, context);
		UniformBuffer.syncAndBind(program);

		var uniformLocation = program.getUniformLocation("_flw_materialID_instancing");
		var vertexID = MaterialIndices.getVertexShaderIndex(material);
		var fragmentID = MaterialIndices.getFragmentShaderIndex(material);
		GL32.glUniform2ui(uniformLocation, vertexID, fragmentID);
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
		info.add("GL33 Instanced Arrays");
		info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
	}
}
