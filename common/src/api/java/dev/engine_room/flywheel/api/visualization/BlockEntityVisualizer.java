package dev.engine_room.flywheel.api.visualization;

import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A visualizer that will be keyed to a block entity type.
 * @param <T> The block entity type.
 */
public interface BlockEntityVisualizer<T extends BlockEntity> {
	/**
	 * Given a block entity and context, constructs a visual for the block entity.
	 *
	 * @param ctx         Context for creating a visual.
	 * @param blockEntity The block entity to construct a visual for.
	 * @return The visual.
	 */
	BlockEntityVisual<? super T> createVisual(VisualizationContext ctx, T blockEntity, float partialTick);

	/**
	 * Checks if the given block entity should not be rendered with the vanilla {@link BlockEntityRenderer}.
	 * @param blockEntity The block entity to check.
	 * @return {@code true} if the block entity should not be rendered with the vanilla {@link BlockEntityRenderer}, {@code false} if it should.
	 */
	boolean skipVanillaRender(T blockEntity);
}
