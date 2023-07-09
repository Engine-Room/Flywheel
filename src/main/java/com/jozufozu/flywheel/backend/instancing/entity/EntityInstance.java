package com.jozufozu.flywheel.backend.instancing.entity;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.light.LightListener;
import com.jozufozu.flywheel.light.TickingLightListener;
import com.jozufozu.flywheel.util.box.GridAlignedBB;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The layer between a {@link BlockEntity} and the Flywheel backend.
 * *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicInstance}</li>
 *     <li>{@link TickableInstance}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give a {@link EntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <E> The type of {@link Entity} your class is an instance of.
 */
public abstract class EntityInstance<E extends Entity> extends AbstractInstance implements LightListener, TickingLightListener {

	protected final E entity;
	protected final GridAlignedBB bounds;

	public EntityInstance(MaterialManager materialManager, E entity) {
		super(materialManager, entity.level());
		this.entity = entity;
		bounds = GridAlignedBB.from(entity.getBoundingBox());
	}

	@Override
	public GridAlignedBB getVolume() {
		return bounds;
	}

	@Override
	public boolean tickLightListener() {
		AABB boundsNow = entity.getBoundingBox();

		if (bounds.sameAs(boundsNow)) return false;

		bounds.assign(boundsNow);

		updateLight();

		return true;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link BlockEntityInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this instance should be rendered at to appear in the correct location.
	 */
	public Vector3f getInstancePosition() {
		Vec3 pos = entity.position();
		Vec3i origin = materialManager.getOriginCoordinate();
		return new Vector3f((float) (pos.x - origin.getX()), (float) (pos.y - origin.getY()), (float) (pos.z - origin.getZ()));
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link BlockEntityInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this instance should be rendered at to appear in the correct location.
	 */
	public Vector3f getInstancePosition(float partialTicks) {
		Vec3 pos = entity.position();
		Vec3i origin = materialManager.getOriginCoordinate();
		return new Vector3f(
				(float) (Mth.lerp(partialTicks, entity.xOld, pos.x) - origin.getX()),
				(float) (Mth.lerp(partialTicks, entity.yOld, pos.y) - origin.getY()),
				(float) (Mth.lerp(partialTicks, entity.zOld, pos.z) - origin.getZ())
		);
	}

	@Override
	public BlockPos getWorldPosition() {
		return entity.blockPosition();
	}
}
