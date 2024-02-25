package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL32;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.CommonCrumbling;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.backend.engine.MaterialEncoder;
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.engine.textures.TextureBinder;
import com.jozufozu.flywheel.backend.engine.textures.TextureSourceImpl;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.gl.TextureBuffer;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.ContextShaders;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.ModelBakery;

public class InstancedDrawManager extends InstancerStorage<InstancedInstancer<?>> {
	/**
	 * The set of draw calls to make in each {@link RenderStage}.
	 */
	private final Map<RenderStage, DrawSet> drawSets = new EnumMap<>(RenderStage.class);
	private final InstancingPrograms programs;
	/**
	 * A map of vertex types to their mesh pools.
	 */
	private final MeshPool meshPool;
	private final GlVertexArray vao;
	private final TextureSourceImpl textures;
	private final TextureBuffer instanceTexture;

	public InstancedDrawManager(InstancingPrograms programs) {
		programs.acquire();
		this.programs = programs;

		meshPool = new MeshPool();
		vao = GlVertexArray.create();
		textures = new TextureSourceImpl();
		instanceTexture = new TextureBuffer();

		meshPool.bind(vao);
	}

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

		for (DrawSet drawSet : drawSets.values()) {
			// Remove the draw calls for any instancers we deleted.
			drawSet.prune();
		}

		meshPool.flush();
	}

	public void renderStage(RenderStage stage) {
		var drawSet = drawSets.getOrDefault(stage, DrawSet.EMPTY);

		if (drawSet.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			render(drawSet);
		}
	}

	public void delete() {
		instancers.values()
				.forEach(InstancedInstancer::delete);

		drawSets.values()
				.forEach(DrawSet::delete);
		drawSets.clear();

		meshPool.delete();
		instanceTexture.delete();
		programs.release();
		vao.delete();

		super.delete();
	}

	private void render(InstancedDrawManager.DrawSet drawSet) {
		Uniforms.bindForDraw();
		vao.bindForDraw();
		TextureBinder.bindLightAndOverlay();

		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			if (drawCalls.isEmpty()) {
				continue;
			}

			var context = shader.context();
			var material = shader.material();

			var program = programs.get(shader.instanceType(), context.contextShader());
			program.bind();

			uploadMaterialUniform(program, material);

			context.prepare(material, program, textures);
			MaterialRenderState.setup(material);

			GlTextureUnit.T3.makeActive();

			program.setSamplerBinding("_flw_instances", 3);

			for (var drawCall : drawCalls) {
				drawCall.render(instanceTexture);
			}
			TextureBinder.resetTextureBindings();
		}

		MaterialRenderState.reset();
		TextureBinder.resetLightAndOverlay();
	}

	@Override
	protected <I extends Instance> InstancedInstancer<I> create(InstancerKey<I> key) {
		return new InstancedInstancer<>(key.type(), key.context());
	}

	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, InstancedInstancer<?> instancer) {
		instancer.init();

		DrawSet drawSet = drawSets.computeIfAbsent(key.stage(), DrawSet::new);

		var meshes = key.model()
				.meshes();
		for (var entry : meshes) {
			var mesh = meshPool.alloc(entry.mesh());

			ShaderState shaderState = new ShaderState(entry.material(), key.type(), key.context());
			DrawCall drawCall = new DrawCall(instancer, mesh, shaderState);

			drawSet.put(shaderState, drawCall);
			instancer.addDrawCall(drawCall);
		}
	}

	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		// Sort draw calls into buckets, so we don't have to do as many shader binds.
		var byShaderState = doCrumblingSort(crumblingBlocks);

		if (byShaderState.isEmpty()) {
			return;
		}

		var crumblingMaterial = SimpleMaterial.builder();

		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.bindForDraw();
			vao.bindForDraw();
			TextureBinder.bindLightAndOverlay();

			for (var shaderStateEntry : byShaderState.entrySet()) {
				var byProgress = shaderStateEntry.getValue();

				if (byProgress.isEmpty()) {
					continue;
				}

				ShaderState shader = shaderStateEntry.getKey();

				CommonCrumbling.applyCrumblingProperties(crumblingMaterial, shader.material());

				var program = programs.get(shader.instanceType(), ContextShaders.CRUMBLING);
				program.bind();

				uploadMaterialUniform(program, crumblingMaterial);

				MaterialRenderState.setup(crumblingMaterial);

				for (var progressEntry : byProgress.int2ObjectEntrySet()) {
					var drawCalls = progressEntry.getValue();

					if (drawCalls.isEmpty()) {
						continue;
					}

					var context = Contexts.CRUMBLING.get(progressEntry.getIntKey());
					context.prepare(crumblingMaterial, program, textures);

					GlTextureUnit.T3.makeActive();
					program.setSamplerBinding("_flw_instances", 3);

					for (Consumer<TextureBuffer> drawCall : drawCalls) {
						drawCall.accept(instanceTexture);
					}

					TextureBinder.resetTextureBindings();
				}
			}

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();
		}
	}

	private static Map<ShaderState, Int2ObjectMap<List<Consumer<TextureBuffer>>>> doCrumblingSort(List<Engine.CrumblingBlock> instances) {
		Map<ShaderState, Int2ObjectMap<List<Consumer<TextureBuffer>>>> out = new HashMap<>();

		for (Engine.CrumblingBlock triple : instances) {
			int progress = triple.progress();

			if (progress < 0 || progress >= ModelBakery.DESTROY_TYPES.size()) {
				continue;
			}

			for (Instance instance : triple.instances()) {
				// Filter out instances that weren't created by this engine.
				// If all is well, we probably shouldn't take the `continue`
				// branches but better to do checked casts.
				if (!(instance.handle() instanceof InstanceHandleImpl impl)) {
					continue;
				}
				if (!(impl.instancer instanceof InstancedInstancer<?> instancer)) {
					continue;
				}

				for (DrawCall draw : instancer.drawCalls()) {
					out.computeIfAbsent(draw.shaderState, $ -> new Int2ObjectArrayMap<>())
							.computeIfAbsent(progress, $ -> new ArrayList<>())
							.add(buf -> draw.renderOne(buf, impl));
				}
			}
		}

		return out;
	}

	public static void uploadMaterialUniform(GlProgram program, Material material) {
		int uniformLocation = program.getUniformLocation("_flw_packedMaterial");
		int vertexIndex = ShaderIndices.getVertexShaderIndex(material.shaders());
		int fragmentIndex = ShaderIndices.getFragmentShaderIndex(material.shaders());
		int packedFogAndCutout = MaterialEncoder.packFogAndCutout(material);
		int packedMaterialProperties = MaterialEncoder.packProperties(material);
		GL32.glUniform4ui(uniformLocation, vertexIndex, fragmentIndex, packedFogAndCutout, packedMaterialProperties);
	}

	public static class DrawSet implements Iterable<Map.Entry<ShaderState, Collection<DrawCall>>> {
		public static final DrawSet EMPTY = new DrawSet(ImmutableListMultimap.of());

		private final ListMultimap<ShaderState, DrawCall> drawCalls;

		public DrawSet(RenderStage renderStage) {
			drawCalls = ArrayListMultimap.create();
		}

		public DrawSet(ListMultimap<ShaderState, DrawCall> drawCalls) {
			this.drawCalls = drawCalls;
		}

		private void delete() {
			drawCalls.values()
					.forEach(DrawCall::delete);
			drawCalls.clear();
		}

		public void put(ShaderState shaderState, DrawCall drawCall) {
			drawCalls.put(shaderState, drawCall);
		}

		public boolean isEmpty() {
			return drawCalls.isEmpty();
		}

		@Override
		public Iterator<Map.Entry<ShaderState, Collection<DrawCall>>> iterator() {
			return drawCalls.asMap()
					.entrySet()
					.iterator();
		}

		public void prune() {
            drawCalls.values()
                    .removeIf(DrawCall::deleted);
		}
	}
}
