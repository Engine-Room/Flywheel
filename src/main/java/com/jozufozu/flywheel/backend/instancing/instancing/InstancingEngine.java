package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.MeshPool;
import com.jozufozu.flywheel.core.CoreShaderInfoMap;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo;
import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.util.Textures;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class InstancingEngine<P extends WorldProgram> implements Engine {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ProgramCompiler<P> context;
	private MeshPool allocator;

	protected final Map<Instanced<? extends InstanceData>, GPUInstancerFactory<?>> factories = new HashMap<>();

	protected final Set<RenderType> toRender = new HashSet<>();

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
	public <D extends InstanceData> GPUInstancerFactory<D> factory(StructType<D> type) {
		if (type instanceof Instanced<D> instanced) {
			return (GPUInstancerFactory<D>) factories.computeIfAbsent(instanced, GPUInstancerFactory::new);
		} else {
			throw new ClassCastException("Cannot use type '" + type + "' with GPU instancing.");
		}
	}

	@Override
	public void renderAllRemaining(TaskEngine taskEngine, RenderContext context) {
		var camX = context.camX() - originCoordinate.getX();
		var camY = context.camY() - originCoordinate.getY();
		var camZ = context.camZ() - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = context.viewProjection().copy();
		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);

		for (RenderType renderType : toRender) {
			render(renderType, camX, camY, camZ, vp, context.level());
		}
		toRender.clear();
	}

	@Override
	public void renderSpecificType(TaskEngine taskEngine, RenderContext context, RenderType type) {
		var camX = context.camX() - originCoordinate.getX();
		var camY = context.camY() - originCoordinate.getY();
		var camZ = context.camZ() - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = context.viewProjection().copy();
		vp.multiplyWithTranslation((float) -camX, (float) -camY, (float) -camZ);

		if (toRender.remove(type)) {
			render(type, camX, camY, camZ, vp, context.level());
		}
	}

	protected void render(RenderType type, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
		vertexCount = 0;
		instanceCount = 0;

		type.setupRenderState();
		Textures.bindActiveTextures();
		CoreShaderInfo coreShaderInfo = getCoreShaderInfo();

		for (Map.Entry<Instanced<? extends InstanceData>, GPUInstancerFactory<?>> entry : factories.entrySet()) {
			Instanced<? extends InstanceData> instanceType = entry.getKey();
			GPUInstancerFactory<?> factory = entry.getValue();

			var materials = factory.materials.get(type);
			for (Material material : materials) {
				var toRender = factory.renderables.get(material);
				toRender.removeIf(Renderable::shouldRemove);

				if (!toRender.isEmpty()) {
					setup(instanceType, material, coreShaderInfo, camX, camY, camZ, viewProjection, level);

					instanceCount += factory.getInstanceCount();
					vertexCount += factory.getVertexCount();

					toRender.forEach(Renderable::render);
				}
			}
		}

		type.clearRenderState();
	}

	protected CoreShaderInfo getCoreShaderInfo() {
		CoreShaderInfo coreShaderInfo;
		ShaderInstance coreShader = RenderSystem.getShader();
		if (coreShader != null) {
			String coreShaderName = coreShader.getName();
			coreShaderInfo = CoreShaderInfoMap.getInfo(coreShaderName);
		} else {
			coreShaderInfo = null;
		}
		if (coreShaderInfo == null) {
			coreShaderInfo = CoreShaderInfo.DEFAULT;
		}
		return coreShaderInfo;
	}

	protected P setup(Instanced<?> instanceType, Material material, CoreShaderInfo coreShaderInfo, double camX, double camY, double camZ, Matrix4f viewProjection, ClientLevel level) {
		float alphaDiscard = coreShaderInfo.alphaDiscard();
		if (alphaDiscard == 0) {
			alphaDiscard = 0.0001f;
		} else if (alphaDiscard < 0) {
			alphaDiscard = 0;
		}

		P program = context.getProgram(new ProgramContext(Formats.POS_TEX_NORMAL, instanceType.getInstanceShader(), material.getVertexShader(), material.getFragmentShader(), alphaDiscard, coreShaderInfo.fogType(), GameStateRegistry.takeSnapshot()));

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

		MeshPool allocator = getModelAllocator();

		for (GPUInstancerFactory<?> factory : factories.values()) {
			factory.init(allocator);

			toRender.addAll(factory.materials.keySet());
		}

		allocator.flush();

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

	private MeshPool getModelAllocator() {
		if (allocator == null) {
			allocator = createAllocator();
		}
		return this.allocator;
	}

	private static MeshPool createAllocator() {

		// FIXME: Windows AMD Drivers don't like ..BaseVertex
		return new MeshPool(Formats.POS_TEX_NORMAL);
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}
}
