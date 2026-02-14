package dev.engine_room.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.InstancingPrograms;
import dev.engine_room.flywheel.backend.compile.PipelineCompiler;
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
import dev.engine_room.flywheel.backend.engine.indirect.OitFramebuffer;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;

public class InstancedDrawManager extends DrawManager<InstancedInstancer<?>> {
	private static final Comparator<InstancedDraw> DRAW_COMPARATOR = Comparator.comparingInt(InstancedDraw::bias)
			.thenComparingInt(InstancedDraw::indexOfMeshInModel)
			.thenComparing(InstancedDraw::material, MaterialRenderState.COMPARATOR);

	private final List<InstancedDraw> allDraws = new ArrayList<>();
	private boolean needSort = false;

	private final List<InstancedDraw> draws = new ArrayList<>();
	private final List<InstancedDraw> oitDraws = new ArrayList<>();

	private final InstancingPrograms programs;
	/**
	 * A map of vertex types to their mesh pools.
	 */
	private final MeshPool meshPool;
	private final GlVertexArray vao;
	private final TextureBuffer instanceTexture;
	private final InstancedLight light;

	private final OitFramebuffer oitFramebuffer;

	public InstancedDrawManager(InstancingPrograms programs) {
		programs.acquire();
		this.programs = programs;

		meshPool = new MeshPool();
		vao = GlVertexArray.create();
		instanceTexture = new TextureBuffer();
		light = new InstancedLight();

		meshPool.bind(vao);

		oitFramebuffer = new OitFramebuffer(programs.oitPrograms());

	}

	@Override
	public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
		super.render(lightStorage, environmentStorage);

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
		needSort |= allDraws.removeIf(InstancedDraw::deleted);

		if (needSort) {
			allDraws.sort(DRAW_COMPARATOR);

			draws.clear();
			oitDraws.clear();

			for (var draw : allDraws) {
				if (draw.material()
						.transparency() == Transparency.ORDER_INDEPENDENT) {
					oitDraws.add(draw);
				} else {
					draws.add(draw);
				}
			}

			needSort = false;
		}

		meshPool.flush();

		light.flush(lightStorage);

		if (allDraws.isEmpty()) {
			return;
		}

		Uniforms.bindAll();
		vao.bindForDraw();
		TextureBinder.bindLightAndOverlay();
		light.bind();

		TextureBinder.bindRenderTarget(Minecraft.getInstance().getMainRenderTarget());


		submitDraws();

		if (!oitDraws.isEmpty()) {
			oitFramebuffer.prepare();

			oitFramebuffer.depthRange();

			submitOitDraws(PipelineCompiler.OitMode.DEPTH_RANGE);

			oitFramebuffer.renderTransmittance();

			submitOitDraws(PipelineCompiler.OitMode.GENERATE_COEFFICIENTS);

			oitFramebuffer.renderDepthFromTransmittance();

			// Need to bind this again because we just drew a full screen quad for OIT.
			vao.bindForDraw();

			oitFramebuffer.accumulate();

			submitOitDraws(PipelineCompiler.OitMode.EVALUATE);

			oitFramebuffer.composite();
		}
	}

	private void submitDraws() {
		for (var drawCall : draws) {
			var material = drawCall.material();
			var groupKey = drawCall.groupKey;
			var environment = groupKey.environment();

			var program = programs.get(groupKey.instanceType(), environment.contextShader(), material, PipelineCompiler.OitMode.OFF);
			program.bind();

			environment.setupDraw(program);

			uploadMaterialUniform(program, material);

			program.setUInt("_flw_baseVertex", drawCall.mesh()
					.baseVertex());

			MaterialRenderState.setup(material);

			Samplers.INSTANCE_BUFFER.makeActive();

			drawCall.render(instanceTexture);
		}
	}

	private void submitOitDraws(PipelineCompiler.OitMode mode) {
		for (var drawCall : oitDraws) {
			var material = drawCall.material();
			var groupKey = drawCall.groupKey;
			var environment = groupKey.environment();

			var program = programs.get(groupKey.instanceType(), environment.contextShader(), material, mode);
			program.bind();

			environment.setupDraw(program);

			uploadMaterialUniform(program, material);

			program.setUInt("_flw_baseVertex", drawCall.mesh()
					.baseVertex());

			MaterialRenderState.setupOit(material);

			Samplers.INSTANCE_BUFFER.makeActive();

			drawCall.render(instanceTexture);
		}
	}

	@Override
	public void delete() {
		instancers.values()
				.forEach(InstancedInstancer::delete);

		allDraws.forEach(InstancedDraw::delete);
		allDraws.clear();
		draws.clear();
		oitDraws.clear();

		meshPool.delete();
		instanceTexture.delete();
		programs.release();
		vao.delete();

		light.delete();

		oitFramebuffer.delete();

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

			allDraws.add(instancedDraw);
			needSort = true;
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
		TextureBinder.bindRenderTarget(Minecraft.getInstance().getMainRenderTarget());

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
						var program = programs.get(shader.instanceType(), ContextShader.CRUMBLING, crumblingMaterial, PipelineCompiler.OitMode.OFF);
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
	}

	@Override
	public void triggerFallback() {
		InstancingPrograms.kill();
		Minecraft.getInstance().levelRenderer.allChanged();
	}

	@Override
	public MeshPool meshPool() {
		return meshPool;
	}

	public static void uploadMaterialUniform(GlProgram program, Material material) {
		int packedFogAndCutout = MaterialEncoder.packUberShader(material);
		int packedMaterialProperties = MaterialEncoder.packProperties(material);
		program.setUVec2("_flw_packedMaterial", packedFogAndCutout, packedMaterialProperties);
	}
}
