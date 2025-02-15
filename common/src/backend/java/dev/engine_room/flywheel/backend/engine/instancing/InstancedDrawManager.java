package dev.engine_room.flywheel.backend.engine.instancing;

import java.util.List;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.InstancingPrograms;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.CommonCrumbling;
import dev.engine_room.flywheel.backend.engine.DrawManager;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.engine.MaterialEncoder;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.engine.TextureBinder;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
	private final InstancedRenderStage draws = new InstancedRenderStage();
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
	public void flush(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
		super.flush(lightStorage, environmentStorage);

		this.instancers.values()
				.removeIf(instancer -> {
			if (instancer.instanceCount() == 0) {
				instancer.delete();
				return true;
			} else {
				instancer.updateBuffer();
				return false;
			}
		});

		// Remove the draw calls for any instancers we deleted.
		draws.flush();

		meshPool.flush();

		light.flush(lightStorage);
	}

	@Override
	public void render() {
		var stage = draws;

		if (stage.isEmpty()) {
			return;
		}

		Uniforms.bindAll();
		vao.bindForDraw();
		TextureBinder.bindLightAndOverlay();
		light.bind();

		stage.draw(instanceTexture, programs);

		MaterialRenderState.reset();
		TextureBinder.resetLightAndOverlay();
	}

	@Override
	public void delete() {
		instancers.values()
				.forEach(InstancedInstancer::delete);

		draws.delete();

		meshPool.delete();
		instanceTexture.delete();
		programs.release();
		vao.delete();

		light.delete();

		super.delete();
	}

	@Override
	protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
		return new InstancedInstancer<>(key, new AbstractInstancer.Recreate<>(key, this));
	}

	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
		instancer.init();

		var meshes = key.model()
				.meshes();
		for (int i = 0; i < meshes.size(); i++) {
			var entry = meshes.get(i);
			var mesh = meshPool.alloc(entry.mesh());

			GroupKey<?> groupKey = new GroupKey<>(key.type(), key.environment());
			InstancedDraw instancedDraw = new InstancedDraw(instancer, mesh, groupKey, entry.material(), key.bias(), i);

			draws.put(groupKey, instancedDraw);
			instancer.addDrawCall(instancedDraw);
		}
	}

	@Override
	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		// Sort draw calls into buckets, so we don't have to do as many shader binds.
		var byType = doCrumblingSort(crumblingBlocks, handle -> {
			// AbstractInstancer directly implement HandleState, so this check is valid.
			if (handle instanceof InstancedInstancer<?> instancer) {
				return instancer;
			}
			// This rejects instances that were created by a different engine,
			// and also instances that are hidden or deleted.
			return null;
		});

		if (byType.isEmpty()) {
			return;
		}

		var crumblingMaterial = SimpleMaterial.builder();

		Uniforms.bindAll();
		vao.bindForDraw();
		TextureBinder.bindLightAndOverlay();

		for (var groupEntry : byType.entrySet()) {
			var byProgress = groupEntry.getValue();

			GroupKey<?> shader = groupEntry.getKey();

			for (var progressEntry : byProgress.int2ObjectEntrySet()) {
				Samplers.CRUMBLING.makeActive();
				TextureBinder.bind(ModelBakery.BREAKING_LOCATIONS.get(progressEntry.getIntKey()));

				for (var instanceHandlePair : progressEntry.getValue()) {
					InstancedInstancer<?> instancer = instanceHandlePair.getFirst();
					var index = instanceHandlePair.getSecond().index;

					for (InstancedDraw draw : instancer.draws()) {
						CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());
						var program = programs.get(shader.instanceType(), ContextShader.CRUMBLING, crumblingMaterial);
						program.bind();
						program.setInt("_flw_baseInstance", index);
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

	@Override
	public void triggerFallback() {
		InstancingPrograms.kill();
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	public static void uploadMaterialUniform(GlProgram program, Material material) {
		int packedFogAndCutout = MaterialEncoder.packUberShader(material);
		int packedMaterialProperties = MaterialEncoder.packProperties(material);
		program.setUVec2("_flw_packedMaterial", packedFogAndCutout, packedMaterialProperties);
	}
}
