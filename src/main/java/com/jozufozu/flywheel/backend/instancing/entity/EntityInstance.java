package com.jozufozu.flywheel.backend.instancing.entity;

import java.util.Arrays;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.light.ILightUpdateListener;
import com.jozufozu.flywheel.light.ListenerStatus;
import com.jozufozu.flywheel.light.Volume;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

/**
 * The layer between a {@link TileEntity} and the Flywheel backend.
 * *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link IDynamicInstance}</li>
 *     <li>{@link ITickableInstance}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give a {@link EntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <E> The type of {@link Entity} your class is an instance of.
 */
public abstract class EntityInstance<E extends Entity> extends AbstractInstance implements ILightUpdateListener {

	protected final E entity;

	public EntityInstance(MaterialManager materialManager, E entity) {
		super(materialManager, entity.level);
		this.entity = entity;

		startListening();
	}

	@Override
	public Volume.Box getVolume() {
		return Volume.box(entity.getBoundingBox());
	}

	@Override
	public ListenerStatus status() {
		return ListenerStatus.UPDATE;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link TileInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this instance should be rendered at to appear in the correct location.
	 */
	public Vector3f getInstancePosition() {
		Vector3d pos = entity.position();
		Vector3i origin = materialManager.getOriginCoordinate();
		return new Vector3f((float) (pos.x - origin.getX()), (float) (pos.y - origin.getY()), (float) (pos.z - origin.getZ()));
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link TileInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The position this instance should be rendered at to appear in the correct location.
	 */
	public Vector3f getInstancePosition(float partialTicks) {
		Vector3d pos = entity.position();
		Vector3i origin = materialManager.getOriginCoordinate();
		return new Vector3f(
				(float) (MathHelper.lerp(partialTicks, entity.xOld, pos.x) - origin.getX()),
				(float) (MathHelper.lerp(partialTicks, entity.yOld, pos.y) - origin.getY()),
				(float) (MathHelper.lerp(partialTicks, entity.zOld, pos.z) - origin.getZ())
		);
	}

	@Override
	public BlockPos getWorldPosition() {
		return entity.blockPosition();
	}
}
