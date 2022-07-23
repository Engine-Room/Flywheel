package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.MeshPool;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo;
import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.compile.ContextShader;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.uniform.UniformBuffer;
import com.jozufozu.flywheel.util.Textures;
import com.jozufozu.flywheel.util.WeakHashSet;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InstancingEngine implements Engine {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ContextShader context;

	protected final Map<StructType<?>, GPUInstancerFactory<?>> factories = new HashMap<>();

	protected final List<InstancedModel<?>> uninitializedModels = new ArrayList<>();
	protected final RenderLists renderLists = new RenderLists();

	/**
	 * The set of instance managers that are attached to this engine.
	 */
	private final WeakHashSet<InstanceManager<?>> instanceManagers;
	private int vertexCount;
	private int instanceCount;

	public InstancingEngine(ContextShader context) {
		this.context = context;

		this.instanceManagers = new WeakHashSet<>();
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
		for (RenderType renderType : renderLists.drainLayers()) {
			render(renderType);
		}
	}

	@Override
	public void renderSpecificType(TaskEngine taskEngine, RenderContext context, RenderType type) {
		if (!renderLists.process(type)) {
			return;
		}

		render(type);
	}

	protected void render(RenderType type) {
		vertexCount = 0;
		instanceCount = 0;

		var multimap = renderLists.get(type);

		if (multimap.isEmpty()) {
			return;
		}

		render(type, multimap);
	}

	protected void render(RenderType type, ListMultimap<ShaderState, DrawCall> multimap) {
		type.setupRenderState();
		Textures.bindActiveTextures();
		CoreShaderInfo coreShaderInfo = CoreShaderInfo.get();
		StateSnapshot state = GameStateRegistry.takeSnapshot();

		for (var entry : multimap.asMap().entrySet()) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::shouldRemove);

			if (drawCalls.isEmpty()) {
				continue;
			}

			setup(shader, coreShaderInfo, state);

			for (var drawCall : drawCalls) {
				drawCall.render();
			}

		}

		type.clearRenderState();
	}

	protected void setup(ShaderState desc, CoreShaderInfo coreShaderInfo, StateSnapshot ctx) {

		VertexType vertexType = desc.vertex();
		FileResolution instanceShader = desc.instance()
				.getInstanceShader();
		Material material = desc.material();

		var program = ProgramCompiler.INSTANCE.getProgram(new ProgramCompiler.Context(vertexType, material,
				instanceShader, context, coreShaderInfo.getAdjustedAlphaDiscard(), coreShaderInfo.fogType(), ctx));

		program.bind();
		UniformBuffer.getInstance().sync();
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

	@Override
	public void attachManagers(InstanceManager<?>... listener) {
		instanceManagers.addAll(List.of(listener));
	}

	@Override
	public boolean maintainOriginCoordinate(Camera camera) {
		Vec3 cameraPos = camera.getPosition();

		double distanceSqr = Vec3.atLowerCornerOf(originCoordinate)
				.subtract(cameraPos)
				.lengthSqr();

		if (distanceSqr > MAX_ORIGIN_DISTANCE * MAX_ORIGIN_DISTANCE) {
			shiftListeners(Mth.floor(cameraPos.x), Mth.floor(cameraPos.y), Mth.floor(cameraPos.z));
			return true;
		}
		return false;
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, Camera info) {
		for (var model : uninitializedModels) {
			model.init(renderLists);
		}
		uninitializedModels.clear();

		renderLists.prepare();

		MeshPool.getInstance()
				.flush();
	}

	private void shiftListeners(int cX, int cY, int cZ) {
		originCoordinate = new BlockPos(cX, cY, cZ);

		factories.values().forEach(GPUInstancerFactory::clear);

		instanceManagers.forEach(InstanceManager::onOriginShift);
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Instances: " + instanceCount);
		info.add("Vertices: " + vertexCount);
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}
}
