package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.CommonCrumbling;
import com.jozufozu.flywheel.backend.engine.DrawManager;
import com.jozufozu.flywheel.backend.engine.GroupKey;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.MaterialEncoder;
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.engine.TextureBinder;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.TextureBuffer;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;

import net.minecraft.client.resources.model.ModelBakery;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
	/**
	 * The set of draw calls to make in each {@link RenderStage}.
	 */
	private final Map<RenderStage, InstancedRenderStage> stages = new EnumMap<>(RenderStage.class);
	private final InstancingPrograms programs;
	/**
	 * A map of vertex types to their mesh pools.
	 */
	private final MeshPool meshPool;
	private final GlVertexArray vao;
	private final TextureBuffer instanceTexture;

	public InstancedDrawManager(InstancingPrograms programs) {
		programs.acquire();
		this.programs = programs;

		meshPool = new MeshPool();
		vao = GlVertexArray.create();
		instanceTexture = new TextureBuffer();

		meshPool.bind(vao);
	}

	@Override
	public void flush() {
		super.flush();

		var instancers = this.instancers.values();
		instancers.removeIf(instancer -> {
			// Update the instancers and remove any that are empty.
			instancer.update();

			if (instancer.instanceCount() == 0) {
				instancer.delete();
				return true;
			} else {
				return false;
			}
		});

		for (InstancedRenderStage instancedRenderStage : stages.values()) {
			// Remove the draw calls for any instancers we deleted.
			instancedRenderStage.flush();
		}

		meshPool.flush();
	}

	@Override
	public void renderStage(RenderStage stage) {
		var drawSet = stages.get(stage);

		if (drawSet == null || drawSet.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.bindForDraw();
			vao.bindForDraw();
			TextureBinder.bindLightAndOverlay();

			drawSet.draw(instanceTexture, programs);

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();
		}
	}

	@Override
	public void delete() {
		instancers.values()
				.forEach(InstancedInstancer::delete);

		stages.values()
				.forEach(InstancedRenderStage::delete);
		stages.clear();

		meshPool.delete();
		instanceTexture.delete();
		programs.release();
		vao.delete();

		super.delete();
	}

	@Override
	protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
		return new InstancedInstancer<>(key.type(), key.environment());
	}

	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
		instancer.init();

		InstancedRenderStage instancedRenderStage = stages.computeIfAbsent(key.stage(), $ -> new InstancedRenderStage());

		var meshes = key.model()
				.meshes();
		for (int i = 0; i < meshes.size(); i++) {
			var entry = meshes.get(i);
			var mesh = meshPool.alloc(entry.mesh());

			GroupKey<?> groupKey = new GroupKey<>(key.type(), key.environment());
			InstancedDraw instancedDraw = new InstancedDraw(instancer, mesh, groupKey, entry.material(), i);

			instancedRenderStage.put(groupKey, instancedDraw);
			instancer.addDrawCall(instancedDraw);
		}
	}

	@Override
	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		// Sort draw calls into buckets, so we don't have to do as many shader binds.
		var byType = doCrumblingSort(InstancedInstancer.class, crumblingBlocks);

		if (byType.isEmpty()) {
			return;
		}

		var crumblingMaterial = SimpleMaterial.builder();

		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.bindForDraw();
			vao.bindForDraw();
			TextureBinder.bindLightAndOverlay();

			for (var groupEntry : byType.entrySet()) {
				var byProgress = groupEntry.getValue();

				GroupKey<?> shader = groupEntry.getKey();

				var program = programs.get(shader.instanceType(), ContextShader.CRUMBLING);
				program.bind();

				for (var progressEntry : byProgress.int2ObjectEntrySet()) {
					Samplers.CRUMBLING.makeActive();
					TextureBinder.bind(ModelBakery.BREAKING_LOCATIONS.get(progressEntry.getIntKey()));

					for (var instanceHandlePair : progressEntry.getValue()) {
						InstancedInstancer<?> instancer = instanceHandlePair.first();
						var handle = instanceHandlePair.second();

						for (InstancedDraw draw : instancer.draws()) {
							CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
							uploadMaterialUniform(program, crumblingMaterial);

							MaterialRenderState.setup(crumblingMaterial);

							Samplers.INSTANCE_BUFFER.makeActive();

							draw.renderOne(instanceTexture, handle);
						}
					}
				}
			}

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();
		}
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
