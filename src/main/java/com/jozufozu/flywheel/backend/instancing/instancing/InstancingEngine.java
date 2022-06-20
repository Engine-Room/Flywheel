package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.model.MeshPool;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo;
import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelSupplier;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.mixin.LevelRendererAccessor;
import com.jozufozu.flywheel.util.Textures;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InstancingEngine<P extends WorldProgram> implements Engine {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ProgramCompiler<P> context;

	protected final Map<StructType<? extends InstancedPart>, GPUInstancerFactory<?>> factories = new HashMap<>();

	protected final List<InstancedModel<?>> uninitializedModels = new ArrayList<>();
	protected final RenderLists renderLists = new RenderLists();

	private final WeakHashSet<OriginShiftListener> listeners;
	private int vertexCount;
	private int instanceCount;

	public InstancingEngine(ProgramCompiler<P> context) {
		this.context = context;

		this.listeners = new WeakHashSet<>();
	}

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public <D extends InstancedPart> GPUInstancerFactory<D> factory(StructType<D> type) {
		return (GPUInstancerFactory<D>) factories.computeIfAbsent(type, this::createFactory);
	}

	@NotNull
	private <D extends InstancedPart> GPUInstancerFactory<D> createFactory(StructType<D> type) {
		return new GPUInstancerFactory<>(type, uninitializedModels::add);
	}

	@Override
	public void renderAllRemaining(TaskEngine taskEngine, RenderContext context) {
		var camX = context.camX() - originCoordinate.getX();
		var camY = context.camY() - originCoordinate.getY();
		var camZ = context.camZ() - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = context.viewProjection().copy();
		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);

		for (RenderType renderType : renderLists.drainLayers()) {
			render(renderType, camX, camY, camZ, vp, context.level());
		}
	}

	@Override
	public void renderSpecificType(TaskEngine taskEngine, RenderContext context, RenderType type) {
		if (!renderLists.process(type)) {
			return;
		}

		var camX = context.camX() - originCoordinate.getX();
		var camY = context.camY() - originCoordinate.getY();
		var camZ = context.camZ() - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = context.viewProjection().copy();
		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);

		render(type, camX, camY, camZ, vp, context.level());
	}

	protected void render(RenderType type, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
		vertexCount = 0;
		instanceCount = 0;

		var multimap = renderLists.get(type);

		if (multimap.isEmpty()) {
			return;
		}

		render(type, multimap, camX, camY, camZ, viewProjection, level);
	}

	protected void render(RenderType type, ListMultimap<ShaderState, DrawCall> multimap, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
		type.setupRenderState();
		Textures.bindActiveTextures();
		CoreShaderInfo coreShaderInfo = CoreShaderInfo.get();

		for (var entry : Multimaps.asMap(multimap).entrySet()) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::shouldRemove);

			if (drawCalls.isEmpty()) {
				continue;
			}

			setup(shader, coreShaderInfo, camX, camY, camZ, viewProjection, level);

			for (var drawCall : drawCalls) {
				drawCall.render();
			}

		}

		type.clearRenderState();
	}

	protected P setup(ShaderState desc, CoreShaderInfo coreShaderInfo, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {

		VertexType vertexType = desc.vertex();
		FileResolution instanceShader = desc.instance()
				.getInstanceShader();
		Material material = desc.material();

		P program = context.getProgram(new ProgramCompiler.Context(vertexType, instanceShader,
				material.vertexShader(), material.fragmentShader(), coreShaderInfo.getAdjustedAlphaDiscard(),
				coreShaderInfo.fogType(), GameStateRegistry.takeSnapshot()));

		program.bind();
		program.uploadUniforms(camX, camY, camZ, viewProjection, level);

		return program;
	}

	public void clearAll() {
		factories.values().forEach(GPUInstancerFactory::clear);
	}

	@Override
	public void delete() {
		factories.values()
				.forEach(GPUInstancerFactory::delete);

		factories.clear();
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return originCoordinate;
	}

	public void addListener(OriginShiftListener listener) {
		listeners.add(listener);
	}

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 *
	 * This prevents floating point precision issues at high coordinates.
	 */
	@Override
	public void beginFrame(Camera info) {
		checkOriginDistance(info);

		for (var factory : uninitializedModels) {
			factory.init(renderLists);
		}
		uninitializedModels.clear();

		renderLists.prepare();

		MeshPool.getInstance()
				.flush();
	}

	private void checkOriginDistance(Camera info) {
		int cX = Mth.floor(info.getPosition().x);
		int cY = Mth.floor(info.getPosition().y);
		int cZ = Mth.floor(info.getPosition().z);

		int dX = cX - originCoordinate.getX();
		int dY = cY - originCoordinate.getY();
		int dZ = cZ - originCoordinate.getZ();

		if (Math.abs(dX) > MAX_ORIGIN_DISTANCE || Math.abs(dY) > MAX_ORIGIN_DISTANCE || Math.abs(dZ) > MAX_ORIGIN_DISTANCE) {
			shiftListeners(cX, cY, cZ);
		}
	}

	private void shiftListeners(int cX, int cY, int cZ) {
		originCoordinate = new BlockPos(cX, cY, cZ);

		factories.values().forEach(GPUInstancerFactory::clear);

		listeners.forEach(OriginShiftListener::onOriginShift);
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Instances: " + instanceCount);
		info.add("Vertices: " + vertexCount);
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}

	public void renderCrumbling(LevelRenderer levelRenderer, ClientLevel level, PoseStack stack, Camera camera, Matrix4f projectionMatrix) {
		var dataByStage = getDataByStage(levelRenderer, level);
		if (dataByStage.isEmpty()) {
			return;
		}

		var map = modelsToParts(dataByStage);
		var stateSnapshot = GameStateRegistry.takeSnapshot();

		Vec3 cameraPosition = camera.getPosition();
		var camX = cameraPosition.x - originCoordinate.getX();
		var camY = cameraPosition.y - originCoordinate.getY();
		var camZ = cameraPosition.z - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = projectionMatrix.copy();
		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);

		GlBuffer instanceBuffer = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);

		GlVertexArray crumblingVAO = new GlVertexArray();

		crumblingVAO.bind();

		// crumblingVAO.bindAttributes();

		for (var entry : map.entrySet()) {
			var model = entry.getKey();
			var parts = entry.getValue();

			if (parts.isEmpty()) {
				continue;
			}

			StructType<?> structType = parts.get(0).type;

			for (var meshEntry : model.get()
					.entrySet()) {
				Material material = meshEntry.getKey();
				Mesh mesh = meshEntry.getValue();

				MeshPool.BufferedMesh bufferedMesh = MeshPool.getInstance()
						.get(mesh);

				if (bufferedMesh == null || !bufferedMesh.isGpuResident()) {
					continue;
				}

				material.renderType().setupRenderState();

				CoreShaderInfo coreShaderInfo = CoreShaderInfo.get();


				CrumblingProgram program = Contexts.CRUMBLING.getProgram(new ProgramCompiler.Context(Formats.POS_TEX_NORMAL,
						structType.getInstanceShader(), material.vertexShader(), material.fragmentShader(),
						coreShaderInfo.getAdjustedAlphaDiscard(), coreShaderInfo.fogType(),
						GameStateRegistry.takeSnapshot()));

				program.bind();
				program.uploadUniforms(camX, camY, camZ, vp, level);

				// bufferedMesh.drawInstances();
			}
		}
	}

	@NotNull
	private Map<ModelSupplier, List<InstancedPart>> modelsToParts(Int2ObjectMap<List<BlockEntityInstance<?>>> dataByStage) {
		var map = new HashMap<ModelSupplier, List<InstancedPart>>();

		for (var entry : dataByStage.int2ObjectEntrySet()) {
			RenderType currentLayer = ModelBakery.DESTROY_TYPES.get(entry.getIntKey());

			// something about when we call this means that the textures are not ready for use on the first frame they should appear
			if (currentLayer == null) {
				continue;
			}

			for (var blockEntityInstance : entry.getValue()) {

				for (var part : blockEntityInstance.getCrumblingParts()) {
					if (part.getOwner() instanceof GPUInstancer instancer) {

						// queue the instances for copying to the crumbling instance buffer
						map.computeIfAbsent(instancer.parent.model, k -> new ArrayList<>()).add(part);
					}
				}
			}
		}
		return map;
	}

	@Nonnull
	private Int2ObjectMap<List<BlockEntityInstance<?>>> getDataByStage(LevelRenderer levelRenderer, ClientLevel level) {
		var destructionProgress = ((LevelRendererAccessor) levelRenderer).flywheel$getDestructionProgress();
		if (destructionProgress.isEmpty()) {
			return Int2ObjectMaps.emptyMap();
		}

		if (!(InstancedRenderDispatcher.getInstanceWorld(level)
				.getBlockEntityInstanceManager() instanceof BlockEntityInstanceManager beim)) {
			return Int2ObjectMaps.emptyMap();
		}

		var dataByStage = new Int2ObjectArrayMap<List<BlockEntityInstance<?>>>();

		for (var entry : destructionProgress.long2ObjectEntrySet()) {
			SortedSet<BlockDestructionProgress> progresses = entry.getValue();

			if (progresses == null || progresses.isEmpty()) {
				continue;
			}

			int progress = progresses.last()
					.getProgress();

			var data = dataByStage.computeIfAbsent(progress, $ -> new ArrayList<>());

			long pos = entry.getLongKey();

			beim.getCrumblingInstances(pos, data);
		}

		return dataByStage;
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}
}
