package com.jozufozu.flywheel.lib.instance;

import java.util.ArrayList;
import java.util.List;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.instance.BlockEntityInstance;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.backend.instancing.manager.BlockEntityInstanceManager;
import com.jozufozu.flywheel.lib.box.ImmutableBox;
import com.jozufozu.flywheel.lib.box.MutableBox;

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
 * <br> Implementing one or more of these will give a {@link AbstractBlockEntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link BlockEntity} your class is an instance of.
 */
public abstract class AbstractBlockEntityInstance<T extends BlockEntity> extends AbstractInstance implements BlockEntityInstance<T> {

	protected final T blockEntity;
	protected final BlockPos pos;
	protected final BlockPos instancePos;
	protected final BlockState blockState;

	public AbstractBlockEntityInstance(InstanceContext ctx, T blockEntity) {
		super(ctx, blockEntity.getLevel());
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.blockState = blockEntity.getBlockState();
		this.instancePos = pos.subtract(renderOrigin);
	}

	@Override
	public List<InstancedPart> getCrumblingParts() {
		var out = new ArrayList<InstancedPart>();
		addCrumblingParts(out);
		return out;
	}

	public void addCrumblingParts(List<InstancedPart> data) {

	}

	/**
	 * Just before {@link #update()} would be called, {@code shouldReset()} is checked.
	 * If this function returns {@code true}, then this instance will be {@link #delete removed},
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
		return pos.subtract(renderOrigin);
	}

	@Override
	public ImmutableBox getVolume() {
		return MutableBox.from(pos);
	}

	@Override
	public boolean checkFrustum(FrustumIntersection frustum) {
		return frustum.testAab(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	@Override
	public double distanceSquared(double x, double y, double z) {
		return pos.distToCenterSqr(x, y, z);
	}
}
