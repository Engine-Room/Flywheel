package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.backend.engine.MaterialEncoder;
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class InstancingEngine extends AbstractEngine {
	private final InstancingPrograms programs;
	private final InstancedDrawManager drawManager = new InstancedDrawManager();
	private final Flag flushFlag = new NamedFlag("flushed");

	public InstancingEngine(InstancingPrograms programs, int maxOriginDistance) {
		super(maxOriginDistance);
		programs.acquire();
		this.programs = programs;
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SyncedPlan.of(this::flushDrawManager);
	}

	private void flushDrawManager(RenderContext ctx) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
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

		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
			int prevActiveTexture = GlStateManager._getActiveTexture();
			gameRenderer.overlayTexture().setupOverlayColor();
			gameRenderer.lightTexture().turnOnLightLayer();

			GlTextureUnit.T1.makeActive();
			RenderSystem.bindTexture(RenderSystem.getShaderTexture(1));
			GlTextureUnit.T2.makeActive();
			RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));

			render(drawSet);

			gameRenderer.overlayTexture().teardownOverlayColor();
			gameRenderer.lightTexture().turnOffLightLayer();
			GlStateManager._activeTexture(prevActiveTexture);
		}
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		// Need to wait for flush before we can inspect instancer state.
		executor.syncUntil(flushFlag::isRaised);

		InstancedCrumbling.render(crumblingBlocks, programs);
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

	private void render(InstancedDrawManager.DrawSet drawSet) {
		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::isInvalid);

			if (drawCalls.isEmpty()) {
				continue;
			}

			var program = programs.get(shader.instanceType(), Contexts.DEFAULT);
			program.bind();

			Uniforms.bindForDraw();
			uploadMaterialUniform(program, shader.material());

			MaterialRenderState.setup(shader.material());

			for (var drawCall : drawCalls) {
				drawCall.render();
			}
		}

		MaterialRenderState.reset();
	}

	public static void uploadMaterialUniform(GlProgram program, Material material) {
		int uniformLocation = program.getUniformLocation("_flw_packedMaterial");
		int vertexIndex = ShaderIndices.getVertexShaderIndex(material.shaders());
		int fragmentIndex = ShaderIndices.getFragmentShaderIndex(material.shaders());
		int packedFogAndCutout = MaterialEncoder.packFogAndCutout(material);
		int packedMaterialProperties = MaterialEncoder.packProperties(material);
		GL32.glUniform4ui(uniformLocation, vertexIndex, fragmentIndex, packedFogAndCutout, packedMaterialProperties);
	}
}
