package com.jozufozu.flywheel.backend.material;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.backend.state.TextureRenderState;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.WeakHashSet;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3i;

public class MaterialManager<P extends WorldProgram> {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final WorldContext<P> context;
	protected final GroupFactory<P> groupFactory;
	protected final boolean ignoreOriginCoordinate;

	protected final Map<RenderLayer, Map<IRenderState, MaterialGroup<P>>> layers;

	private final WeakHashSet<OriginShiftListener> listeners;

	public MaterialManager(WorldContext<P> context) {
		this(context, MaterialGroup::new, false);
	}

	public static <P extends WorldProgram> Builder<P> builder(WorldContext<P> context) {
		return new Builder<>(context);
	}

	public MaterialManager(WorldContext<P> context, GroupFactory<P> groupFactory, boolean ignoreOriginCoordinate) {
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
	 * Render every model for every material.
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
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

		for (Map.Entry<IRenderState, MaterialGroup<P>> entry : layers.get(layer).entrySet()) {
			IRenderState state = entry.getKey();
			MaterialGroup<P> group = entry.getValue();

			state.bind();
			group.render(viewProjection, camX, camY, camZ);
			state.unbind();
		}
	}

	public void delete() {
		for (Map<IRenderState, MaterialGroup<P>> groups : layers.values()) {

			groups.values().forEach(MaterialGroup::delete);
		}
	}

	public MaterialGroup<P> state(RenderLayer layer, IRenderState state) {
		return layers.get(layer).computeIfAbsent(state, this::createGroup);
	}

	public MaterialGroup<P> solid(IRenderState state) {
		return layers.get(RenderLayer.SOLID).computeIfAbsent(state, this::createGroup);
	}

	public MaterialGroup<P> cutout(IRenderState state) {
		return layers.get(RenderLayer.CUTOUT).computeIfAbsent(state, this::createGroup);
	}

	public MaterialGroup<P> transparent(IRenderState state) {
		return layers.get(RenderLayer.TRANSPARENT).computeIfAbsent(state, this::createGroup);
	}

	public MaterialGroup<P> defaultSolid() {
		return solid(TextureRenderState.get(PlayerContainer.BLOCK_ATLAS));
	}

	public MaterialGroup<P> defaultCutout() {
		return cutout(TextureRenderState.get(PlayerContainer.BLOCK_ATLAS));
	}

	public MaterialGroup<P> defaultTransparent() {
		return transparent(TextureRenderState.get(PlayerContainer.BLOCK_ATLAS));
	}

	@Deprecated
	public <D extends InstanceData> InstanceMaterial<D> getMaterial(MaterialSpec<D> materialType) {
		return defaultCutout().material(materialType);
	}

	@Deprecated
	public <D extends InstanceData> InstanceMaterial<D> getMaterial(MaterialSpec<D> materialType, ResourceLocation texture) {
		return cutout(TextureRenderState.get(texture)).material(materialType);
	}

	@Deprecated
	public InstanceMaterial<ModelData> getTransformMaterial() {
		return defaultCutout().material(Materials.TRANSFORMED);
	}

	@Deprecated
	public InstanceMaterial<OrientedData> getOrientedMaterial() {
		return defaultCutout().material(Materials.ORIENTED);
	}

	public Supplier<P> getProgram(ResourceLocation name) {
		return context.getProgramSupplier(name);
	}

	public Vector3i getOriginCoordinate() {
		return originCoordinate;
	}

	public void addListener(OriginShiftListener listener) {
		listeners.add(listener);
	}

	public void checkAndShiftOrigin(ActiveRenderInfo info) {
		int cX = MathHelper.floor(info.getPosition().x);
		int cY = MathHelper.floor(info.getPosition().y);
		int cZ = MathHelper.floor(info.getPosition().z);

		int dX = cX - originCoordinate.getX();
		int dY = cY - originCoordinate.getY();
		int dZ = cZ - originCoordinate.getZ();

		if (Math.abs(dX) > MAX_ORIGIN_DISTANCE || Math.abs(dY) > MAX_ORIGIN_DISTANCE || Math.abs(dZ) > MAX_ORIGIN_DISTANCE) {

			originCoordinate = new BlockPos(cX, cY, cZ);

			for (Map<IRenderState, MaterialGroup<P>> groups : layers.values()) {
				groups.values().forEach(MaterialGroup::clear);
			}

			listeners.forEach(OriginShiftListener::onOriginShift);
		}
	}

	private MaterialGroup<P> createGroup(IRenderState state) {
		return groupFactory.create(this, state);
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}

	@FunctionalInterface
	public interface GroupFactory<P extends WorldProgram> {
		MaterialGroup<P> create(MaterialManager<P> materialManager, IRenderState state);
	}

	public static class Builder<P extends WorldProgram> {
		protected final WorldContext<P> context;
		protected GroupFactory<P> groupFactory = MaterialGroup::new;
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

		public MaterialManager<P> build() {
			return new MaterialManager<>(context, groupFactory, ignoreOriginCoordinate);
		}
	}
}
