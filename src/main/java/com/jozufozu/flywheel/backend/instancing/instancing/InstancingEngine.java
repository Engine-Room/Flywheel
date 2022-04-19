package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Instanced;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.FallbackAllocator;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.RenderTypeRegistry;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.Textures;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class InstancingEngine<P extends WorldProgram> implements Engine {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ProgramCompiler<P> context;
	private ModelAllocator allocator;

	protected final Map<Instanced<? extends InstanceData>, InstancedMaterial<?>> materials = new HashMap<>();

	private final WeakHashSet<OriginShiftListener> listeners;
	private int vertexCount;
	private int instanceCount;

	public InstancingEngine(ProgramCompiler<P> context) {
		this.context = context;

		this.listeners = new WeakHashSet<>();
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	@Override
	public <D extends InstanceData> InstancedMaterial<D> material(StructType<D> type) {
		if (type instanceof Instanced<D> instanced) {
			return (InstancedMaterial<D>) materials.computeIfAbsent(instanced, InstancedMaterial::new);
		} else {
			throw new ClassCastException("Cannot use type '" + type + "' with GPU instancing.");
		}
	}

	@Override
	public void render(TaskEngine taskEngine, RenderContext context) {

		var camX = context.camX() - originCoordinate.getX();
		var camY = context.camY() - originCoordinate.getY();
		var camZ = context.camZ() - originCoordinate.getZ();

		// don't want to mutate viewProjection
		var vp = context.viewProjection().copy();
		vp.multiply(Matrix4f.createTranslateMatrix((float) -camX, (float) -camY, (float) -camZ));

		render(context.type(), camX, camY, camZ, vp);
	}

	protected void render(RenderType type, double camX, double camY, double camZ, Matrix4f viewProjection) {
		vertexCount = 0;
		instanceCount = 0;

		type.setupRenderState();
		Textures.bindActiveTextures();

		for (Map.Entry<Instanced<? extends InstanceData>, InstancedMaterial<?>> entry : materials.entrySet()) {
			InstancedMaterial<?> material = entry.getValue();

			//if (material.anythingToRender(type)) {
				Instanced<? extends InstanceData> instanceType = entry.getKey();

				setup(type, camX, camY, camZ, viewProjection, instanceType.getProgramSpec());

				instanceCount += material.getInstanceCount();
				vertexCount += material.getVertexCount();

				material.renderIn(type);
			//}
		}

		type.clearRenderState();
	}

	protected P setup(RenderType layer, double camX, double camY, double camZ, Matrix4f viewProjection, ResourceLocation programSpec) {
		P program = context.getProgram(ProgramContext.create(programSpec, Formats.POS_TEX_NORMAL, RenderTypeRegistry.getAlphaDiscard(layer)));

		program.bind();
		program.uploadViewProjection(viewProjection);
		program.uploadCameraPos(camX, camY, camZ);

		return program;
	}

	public void clearAll() {
		materials.values().forEach(InstancedMaterial::clear);
	}

	@Override
	public void delete() {
		materials.values()
				.forEach(InstancedMaterial::delete);

		materials.clear();
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

		ModelAllocator allocator = getModelAllocator();

		for (InstancedMaterial<?> material : materials.values()) {
			material.init(allocator);
		}

		if (allocator instanceof ModelPool pool) {
			// ...and then flush the model arena in case anything was marked for upload
			pool.flush();
		}

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

		materials.values().forEach(InstancedMaterial::clear);

		listeners.forEach(OriginShiftListener::onOriginShift);
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Instances: " + instanceCount);
		info.add("Vertices: " + vertexCount);
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}

	private ModelAllocator getModelAllocator() {
		if (allocator == null) {
			allocator = createAllocator();
		}
		return this.allocator;
	}

	private static ModelAllocator createAllocator() {
		if (GlCompat.getInstance()
				.onAMDWindows()) {
			return FallbackAllocator.INSTANCE;
		} else {
			return new ModelPool(Formats.POS_TEX_NORMAL);
		}
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}
}
