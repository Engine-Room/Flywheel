package com.jozufozu.flywheel.lib.visual;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.visual.BlockEntityVisual;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.impl.visualization.manager.BlockEntityVisualManager;
import com.jozufozu.flywheel.lib.box.ImmutableBox;
import com.jozufozu.flywheel.lib.box.MutableBox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The layer between a {@link BlockEntity} and the Flywheel backend.
 *
 * <br><br> {@link #updateLight()} is called after initialization.
 *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link DynamicVisual}</li>
 *     <li>{@link TickableVisual}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give an {@link AbstractBlockEntityVisual} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link BlockEntity}.
 */
public abstract class AbstractBlockEntityVisual<T extends BlockEntity> extends AbstractVisual implements BlockEntityVisual<T> {
	protected final T blockEntity;
	protected final BlockPos pos;
	protected final BlockPos visualPos;
	protected final BlockState blockState;

	public AbstractBlockEntityVisual(VisualizationContext ctx, T blockEntity) {
		super(ctx, blockEntity.getLevel());
		this.blockEntity = blockEntity;
		this.pos = blockEntity.getBlockPos();
		this.blockState = blockEntity.getBlockState();
		this.visualPos = pos.subtract(renderOrigin);
	}

	@Override
	public boolean shouldReset() {
		return blockEntity.getBlockState() != blockState;
	}

	@Override
	public ImmutableBox getVolume() {
		return MutableBox.from(pos);
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link BlockEntityVisualManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The {@link BlockPos position} of the {@link BlockEntity} this visual
	 * represents should be rendered at to appear in the correct location.
	 */
	public BlockPos getVisualPosition() {
		return visualPos;
	}

	/**
	 * @param frustum The current frustum.
	 * @return {@code true} if this visual within the given frustum.
	 */
	public boolean visible(FrustumIntersection frustum) {
		return frustum.testAab(visualPos.getX(), visualPos.getY(), visualPos.getZ(), visualPos.getX() + 1, visualPos.getY() + 1, visualPos.getZ() + 1);
	}

	/**
	 * Limits which frames this visual is updated on based on its distance from the camera.
	 * <p>
	 * You may optionally do this check to avoid updating your visual every frame when it is far away.
	 *
	 * @param context The current frame context.
	 * @return {@code true} if this visual shouldn't be updated this frame based on its distance from the camera.
	 */
	public boolean doDistanceLimitThisFrame(VisualFrameContext context) {
		return !context.limiter()
				.shouldUpdate(pos.distToCenterSqr(context.cameraX(), context.cameraY(), context.cameraZ()));
	}
}
