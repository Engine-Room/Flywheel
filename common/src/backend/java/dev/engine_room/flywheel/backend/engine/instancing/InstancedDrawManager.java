package dev.engine_room.flywheel.backend.engine.instancing;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL32;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.MaterialShaderIndices;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.InstancingPrograms;
import dev.engine_room.flywheel.backend.engine.CommonCrumbling;
import dev.engine_room.flywheel.backend.engine.DrawManager;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.engine.MaterialEncoder;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.engine.TextureBinder;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.resources.model.ModelBakery;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
	/**
	 * The set of draw calls to make for each {@link VisualType}.
	 */
	private final Map<VisualType, InstancedRenderStage> stages = new EnumMap<>(VisualType.class);
	private final InstancingPrograms programs;
	/**
	 * A map of vertex types to their mesh pools.
	 */
	private final MeshPool meshPool;
	private final GlVertexArray vao;
	private final TextureBuffer instanceTexture;
	private final InstancedLight light;

	public InstancedDrawManager(InstancingPrograms programs) {
		programs.acquire();
		this.programs = programs;

		meshPool = new MeshPool();
		vao = GlVertexArray.create();
		instanceTexture = new TextureBuffer();
		light = new InstancedLight();

		meshPool.bind(vao);
	}

	@Override
	public void flush(LightStorage lightStorage) {
		super.flush(lightStorage);

		this.instancers.values()
				.removeIf(instancer -> {
			// Update the instancers and remove any that are empty.
			instancer.update();

			if (instancer.instanceCount() == 0) {
				instancer.delete();
				return true;
			} else {
				return false;
			}
		});

		for (InstancedRenderStage stage : stages.values()) {
			// Remove the draw calls for any instancers we deleted.
			stage.flush();
		}

		meshPool.flush();

		light.flush(lightStorage);
	}

	@Override
	public void render(VisualType visualType) {
		var stage = stages.get(visualType);

		if (stage == null || stage.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.bindAll();
			vao.bindForDraw();
			TextureBinder.bindLightAndOverlay();
			light.bind();

			stage.draw(instanceTexture, programs);

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

		light.delete();

		super.delete();
	}

	@Override
	protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
		return new InstancedInstancer<>(key.type(), key.environment());
	}

	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
		instancer.init();

		InstancedRenderStage stage = stages.computeIfAbsent(key.visualType(), $ -> new InstancedRenderStage());

		var meshes = key.model()
				.meshes();
		for (int i = 0; i < meshes.size(); i++) {
			var entry = meshes.get(i);
			var mesh = meshPool.alloc(entry.mesh());

			GroupKey<?> groupKey = new GroupKey<>(key.type(), key.environment());
			InstancedDraw instancedDraw = new InstancedDraw(instancer, mesh, groupKey, entry.material(), key.bias(), i);

			stage.put(groupKey, instancedDraw);
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
			Uniforms.bindAll();
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
						var index = instanceHandlePair.second().index;

						program.setInt("_flw_baseInstance", index);

						for (InstancedDraw draw : instancer.draws()) {
							CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
							uploadMaterialUniform(program, crumblingMaterial);

							MaterialRenderState.setup(crumblingMaterial);

							Samplers.INSTANCE_BUFFER.makeActive();

							draw.renderOne(instanceTexture);
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
		int vertexIndex = MaterialShaderIndices.vertexIndex(material.shaders());
		int fragmentIndex = MaterialShaderIndices.fragmentIndex(material.shaders());
		int packedFogAndCutout = MaterialEncoder.packFogAndCutout(material);
		int packedMaterialProperties = MaterialEncoder.packProperties(material);
		GL32.glUniform4ui(uniformLocation, vertexIndex, fragmentIndex, packedFogAndCutout, packedMaterialProperties);
	}
}
