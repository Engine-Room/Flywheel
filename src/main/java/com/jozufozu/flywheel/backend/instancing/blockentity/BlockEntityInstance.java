package com.jozufozu.flywheel.backend.instancing.blockentity;

import com.jozufozu.flywheel.api.InstancerFactory;
import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.structs.model.ModelData;
import com.jozufozu.flywheel.core.structs.oriented.OrientedData;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The layer between a {@link BlockEntity} and the Flywheel backend.
 *
 * <br><br> {@link #updateLight()} is called after construction.
 *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicInstance}</li>
 *     <li>{@link TickableInstance}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give a {@link BlockEntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link BlockEntity} your class is an instance of.
 */
public abstract class BlockEntityInstance<T extends BlockEntity> extends AbstractInstance {

	protected final T blockEntity;
	protected final BlockPos pos;
	protected final BlockPos instancePos;
	protected final BlockState blockState;

	public BlockEntityInstance(InstancerManager instancerManager, T blockEntity) {
		super(instancerManager, blockEntity.getLevel());
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.blockState = blockEntity.getBlockState();
		this.instancePos = pos.subtract(instancerManager.getOriginCoordinate());
	}

	/**
	 * Just before {@link #update()} would be called, {@code shouldReset()} is checked.
	 * If this function returns {@code true}, then this instance will be {@link #remove removed},
	 * and another instance will be constructed to replace it. This allows for more sane resource
	 * acquisition compared to trying to update everything within the lifetime of an instance.
	 *
	 * @return {@code true} if this instance should be discarded and refreshed.
	 */
	public boolean shouldReset() {
		return blockEntity.getBlockState() != blockState;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link BlockEntityInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The {@link BlockPos position} of the {@link BlockEntity} this instance
	 * represents should be rendered at to appear in the correct location.
	 */
	public BlockPos getInstancePosition() {
		return pos.subtract(instancerManager.getOriginCoordinate());
	}

	@Override
	public BlockPos getWorldPosition() {
		return pos;
	}

	protected InstancerFactory<ModelData> getTransformFactory() {
        return instancerManager.factory(StructTypes.MODEL);
    }

	protected InstancerFactory<OrientedData> getOrientedFactory() {
        return instancerManager.factory(StructTypes.ORIENTED);
	}

	@Override
	public ImmutableBox getVolume() {
		return GridAlignedBB.from(pos);
	}
}
