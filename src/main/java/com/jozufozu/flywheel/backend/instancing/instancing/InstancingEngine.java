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
import com.jozufozu.flywheel.backend.instancing.Renderable;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.FallbackAllocator;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.ModelPool;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.Textures;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class InstancingEngine<P extends WorldProgram> implements Engine {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ProgramCompiler<P> context;
	protected final GroupFactory<P> groupFactory;
	protected final boolean ignoreOriginCoordinate;
	private ModelAllocator allocator;

	private final Map<Instanced<? extends InstanceData>, InstancedMaterial<?>> materials = new HashMap<>();

	private final WeakHashSet<OriginShiftListener> listeners;
	private int vertexCount;
	private int instanceCount;

	public static <P extends WorldProgram> Builder<P> builder(ProgramCompiler<P> context) {
		return new Builder<>(context);
	}

	public InstancingEngine(ProgramCompiler<P> context, GroupFactory<P> groupFactory, boolean ignoreOriginCoordinate) {
		this.context = context;
		this.ignoreOriginCoordinate = ignoreOriginCoordinate;

		this.listeners = new WeakHashSet<>();
		this.groupFactory = groupFactory;
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
	public void render(TaskEngine taskEngine, RenderLayerEvent event) {

		RenderType type = event.getType();

		double camX;
		double camY;
		double camZ;
		Matrix4f viewProjection;
		if (!ignoreOriginCoordinate) {
			camX = event.camX - originCoordinate.getX();
			camY = event.camY - originCoordinate.getY();
			camZ = event.camZ - originCoordinate.getZ();

			viewProjection = Matrix4f.createTranslateMatrix((float) -camX, (float) -camY, (float) -camZ);
			viewProjection.multiplyBackward(event.viewProjection);
		} else {
			camX = event.camX;
			camY = event.camY;
			camZ = event.camZ;
			viewProjection = event.viewProjection;
		}

		vertexCount = 0;
		instanceCount = 0;

		type.setupRenderState();
		Textures.bindActiveTextures();

		for (Map.Entry<Instanced<? extends InstanceData>, InstancedMaterial<?>> entry : materials.entrySet()) {
			List<Renderable> renderables = entry.getValue()
					.getRenderables(type);

			if (renderables == null || renderables.isEmpty()) {
				continue;
			}

			P program = context.getProgram(ProgramContext.create(entry.getKey()
					.getProgramSpec(), Formats.POS_TEX_NORMAL, event.layer));

			program.bind();
			program.uploadViewProjection(viewProjection);
			program.uploadCameraPos(camX, camY, camZ);

			//setup(program);
			for (Renderable renderable : renderables) {
				renderable.draw();
			}
		}

		type.clearRenderState();
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
		int cX = Mth.floor(info.getPosition().x);
		int cY = Mth.floor(info.getPosition().y);
		int cZ = Mth.floor(info.getPosition().z);

		int dX = cX - originCoordinate.getX();
		int dY = cY - originCoordinate.getY();
		int dZ = cZ - originCoordinate.getZ();

		if (Math.abs(dX) > MAX_ORIGIN_DISTANCE || Math.abs(dY) > MAX_ORIGIN_DISTANCE || Math.abs(dZ) > MAX_ORIGIN_DISTANCE) {

			originCoordinate = new BlockPos(cX, cY, cZ);

			materials.values().forEach(InstancedMaterial::clear);

			listeners.forEach(OriginShiftListener::onOriginShift);
		}

		ModelAllocator allocator = getModelAllocator();

		for (InstancedMaterial<?> material : materials.values()) {
			material.init(allocator);
		}

		if (allocator instanceof ModelPool pool) {
			// ...and then flush the model arena in case anything was marked for upload
			pool.flush();
		}

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

	@FunctionalInterface
	public interface GroupFactory<P extends WorldProgram> {
		InstancedMaterialGroup<P> create(InstancingEngine<P> engine, RenderType type);
	}

	public static class Builder<P extends WorldProgram> {
		protected final ProgramCompiler<P> context;
		protected GroupFactory<P> groupFactory = InstancedMaterialGroup::new;
		protected boolean ignoreOriginCoordinate;

		public Builder(ProgramCompiler<P> context) {
			this.context = context;
		}

		public Builder<P> setGroupFactory(GroupFactory<P> groupFactory) {
			this.groupFactory = groupFactory;
			return this;
		}

		public Builder<P> setIgnoreOriginCoordinate(boolean ignoreOriginCoordinate) {
			this.ignoreOriginCoordinate = ignoreOriginCoordinate;
			return this;
		}

		public InstancingEngine<P> build() {
			return new InstancingEngine<>(context, groupFactory, ignoreOriginCoordinate);
		}
	}
}
