package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.RenderLayerEvent;
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

	protected final Map<RenderLayer, Map<RenderType, InstancedMaterialGroup<P>>> layers;

	private final WeakHashSet<OriginShiftListener> listeners;

	public static <P extends WorldProgram> Builder<P> builder(ProgramCompiler<P> context) {
		return new Builder<>(context);
	}

	public InstancingEngine(ProgramCompiler<P> context, GroupFactory<P> groupFactory, boolean ignoreOriginCoordinate) {
		this.context = context;
		this.ignoreOriginCoordinate = ignoreOriginCoordinate;

		this.listeners = new WeakHashSet<>();
		this.groupFactory = groupFactory;

		this.layers = new EnumMap<>(RenderLayer.class);
		for (RenderLayer value : RenderLayer.values()) {
			layers.put(value, new HashMap<>());
		}
	}

	/**
	 * Get a material group that will render in the given layer with the given type.
	 *
	 * @param layer The {@link RenderLayer} you want to draw in.
	 * @param type The {@link RenderType} you need to draw with.
	 * @return A material group whose children will
	 */
	@Override
	public MaterialGroup state(RenderLayer layer, RenderType type) {
		return layers.get(layer).computeIfAbsent(type, t -> groupFactory.create(this, t));
	}

	/**
	 * Render every model for every material.
	 */
	@Override
	public void render(TaskEngine taskEngine, RenderLayerEvent event) {
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

		getGroupsToRender(event.getLayer()).forEach(group -> group.render(viewProjection, camX, camY, camZ, event.getLayer()));
	}

	private Stream<InstancedMaterialGroup<P>> getGroupsToRender(@Nullable RenderLayer layer) {
		// layer is null when this is called from CrumblingRenderer
		if (layer != null) {
			return layers.get(layer)
					.values()
					.stream();
		} else {
			return layers.values()
					.stream()
					.flatMap(it -> it.values()
							.stream());
		}
	}

	@Override
	public void delete() {
		for (Map<RenderType, InstancedMaterialGroup<P>> groups : layers.values()) {

			groups.values().forEach(InstancedMaterialGroup::delete);
		}
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

			for (Map<RenderType, InstancedMaterialGroup<P>> groups : layers.values()) {
				groups.values().forEach(InstancedMaterialGroup::clear);
			}

			listeners.forEach(OriginShiftListener::onOriginShift);
		}
	}

	@Override
	public String getName() {
		return "GL33 Instanced Arrays";
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
