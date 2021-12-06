package com.jozufozu.flywheel.backend.material;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MaterialManagerImpl<P extends WorldProgram> implements MaterialManager {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final WorldContext<P> context;
	protected final GroupFactory<P> groupFactory;
	protected final boolean ignoreOriginCoordinate;

	protected final Map<RenderLayer, Map<RenderType, MaterialGroupImpl<P>>> layers;

	private final WeakHashSet<OriginShiftListener> listeners;

	public MaterialManagerImpl(WorldContext<P> context) {
		this(context, MaterialGroupImpl::new, false);
	}

	public static <P extends WorldProgram> Builder<P> builder(WorldContext<P> context) {
		return new Builder<>(context);
	}

	public MaterialManagerImpl(WorldContext<P> context, GroupFactory<P> groupFactory, boolean ignoreOriginCoordinate) {
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
	 * Get a material group that will render in the given layer with the given state.
	 *
	 * @param layer The {@link RenderLayer} you want to draw in.
	 * @param state The {@link RenderType} you need to draw with.
	 * @return A material group whose children will
	 */
	@Override
	public MaterialGroup state(RenderLayer layer, RenderType state) {
		return layers.get(layer).computeIfAbsent(state, $ -> groupFactory.create(this));
	}

	/**
	 * Render every model for every material.
	 * @param layer          Which of the 3 {@link RenderLayer render layers} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 */
	public void render(RenderLayer layer, Matrix4f viewProjection, double camX, double camY, double camZ) {
		if (!ignoreOriginCoordinate) {
			camX -= originCoordinate.getX();
			camY -= originCoordinate.getY();
			camZ -= originCoordinate.getZ();

			Matrix4f translate = Matrix4f.createTranslateMatrix((float) -camX, (float) -camY, (float) -camZ);

			translate.multiplyBackward(viewProjection);

			viewProjection = translate;
		}

		for (Map.Entry<RenderType, MaterialGroupImpl<P>> entry : layers.get(layer).entrySet()) {
			RenderType state = entry.getKey();
			MaterialGroupImpl<P> group = entry.getValue();

			group.render(state, viewProjection, camX, camY, camZ);
		}
	}

	public void delete() {
		for (Map<RenderType, MaterialGroupImpl<P>> groups : layers.values()) {

			groups.values().forEach(MaterialGroupImpl::delete);
		}
	}

	public Supplier<P> getProgram(ResourceLocation name) {
		return context.getProgramSupplier(name);
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
	public void beginFrame(Camera info) {
		int cX = Mth.floor(info.getPosition().x);
		int cY = Mth.floor(info.getPosition().y);
		int cZ = Mth.floor(info.getPosition().z);

		int dX = cX - originCoordinate.getX();
		int dY = cY - originCoordinate.getY();
		int dZ = cZ - originCoordinate.getZ();

		if (Math.abs(dX) > MAX_ORIGIN_DISTANCE || Math.abs(dY) > MAX_ORIGIN_DISTANCE || Math.abs(dZ) > MAX_ORIGIN_DISTANCE) {

			originCoordinate = new BlockPos(cX, cY, cZ);

			for (Map<RenderType, MaterialGroupImpl<P>> groups : layers.values()) {
				groups.values().forEach(MaterialGroupImpl::clear);
			}

			listeners.forEach(OriginShiftListener::onOriginShift);
		}
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}

	@FunctionalInterface
	public interface GroupFactory<P extends WorldProgram> {
		MaterialGroupImpl<P> create(MaterialManagerImpl<P> materialManager);
	}

	public static class Builder<P extends WorldProgram> {
		protected final WorldContext<P> context;
		protected GroupFactory<P> groupFactory = MaterialGroupImpl::new;
		protected boolean ignoreOriginCoordinate;

		public Builder(WorldContext<P> context) {
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

		public MaterialManagerImpl<P> build() {
			return new MaterialManagerImpl<>(context, groupFactory, ignoreOriginCoordinate);
		}
	}
}
