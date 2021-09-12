package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.material.Material;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.ImmutableBox;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * The layer between a {@link TileEntity} and the Flywheel backend.
 *
 * <br><br> {@link #updateLight()} is called after construction.
 *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link IDynamicInstance}</li>
 *     <li>{@link ITickableInstance}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give a {@link TileEntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link TileEntity} your class is an instance of.
 */
public abstract class TileEntityInstance<T extends TileEntity> extends AbstractInstance {

	protected final T tile;
	protected final BlockPos pos;
	protected final BlockPos instancePos;
	protected final BlockState blockState;

	public TileEntityInstance(MaterialManager materialManager, T tile) {
		super(materialManager, tile.getLevel());
		this.tile = tile;
		this.pos = tile.getBlockPos();
		this.blockState = tile.getBlockState();
		this.instancePos = pos.subtract(materialManager.getOriginCoordinate());
	}

	/**
	 * Just before {@link #update()} would be called, <code>shouldReset()</code> is checked.
	 * If this function returns <code>true</code>, then this instance will be {@link #remove removed},
	 * and another instance will be constructed to replace it. This allows for more sane resource
	 * acquisition compared to trying to update everything within the lifetime of an instance.
	 *
	 * @return <code>true</code> if this instance should be discarded and refreshed.
	 */
	public boolean shouldReset() {
		return tile.getBlockState() != blockState;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link TileInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The {@link BlockPos position} of the {@link TileEntity} this instance
	 * represents should be rendered at to appear in the correct location.
	 */
	public BlockPos getInstancePosition() {
		return pos.subtract(materialManager.getOriginCoordinate());
	}

	@Override
	public BlockPos getWorldPosition() {
		return pos;
	}

	protected Material<ModelData> getTransformMaterial() {
        return materialManager.defaultCutout().material(Materials.TRANSFORMED);
    }

	protected Material<OrientedData> getOrientedMaterial() {
		return materialManager.defaultCutout().material(Materials.ORIENTED);
	}

	@Override
	public ImmutableBox getVolume() {
		return GridAlignedBB.from(pos);
	}
}
