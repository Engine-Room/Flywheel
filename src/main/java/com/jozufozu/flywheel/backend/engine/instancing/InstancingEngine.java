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
import com.jozufozu.flywheel.backend.engine.textures.TextureBinder;
import com.jozufozu.flywheel.backend.engine.textures.TextureSourceImpl;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;

public class InstancingEngine extends AbstractEngine {
	private final InstancingPrograms programs;
	private final TextureSourceImpl textures = new TextureSourceImpl();
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
			render(drawSet);
		}
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		// Need to wait for flush before we can inspect instancer state.
		executor.syncUntil(flushFlag::isRaised);

		InstancedCrumbling.render(crumblingBlocks, programs, textures);
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
		TextureBinder.bindLightAndOverlay();

		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::isInvalid);

			if (drawCalls.isEmpty()) {
				continue;
			}

			var context = shader.context();
			var material = shader.material();

			var program = programs.get(shader.instanceType(), context.contextShader());
			program.bind();

			Uniforms.bindForDraw();
			uploadMaterialUniform(program, material);

			context.prepare(material, program, textures);
			MaterialRenderState.setup(material);

			for (var drawCall : drawCalls) {
				drawCall.render();
			}
			TextureBinder.resetTextureBindings();
		}

		MaterialRenderState.reset();
		TextureBinder.resetLightAndOverlay();
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
