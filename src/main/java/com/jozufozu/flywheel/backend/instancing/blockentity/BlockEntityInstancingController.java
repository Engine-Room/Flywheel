package com.jozufozu.flywheel.backend.instancing.blockentity;

import com.jozufozu.flywheel.api.MaterialManager;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * An instancing controller that will be keyed to a block entity type.
 * @param <T> The type of block entity this controller is for.
 */
public interface BlockEntityInstancingController<T extends BlockEntity> {
	/**
	 * Given a block entity and a material manager, constructs an instance for the block entity.
	 * @param materialManager The material manager to use.
	 * @param blockEntity The block entity to construct an instance for.
	 * @return The instance.
	 */
	BlockEntityInstance<? super T> createInstance(MaterialManager materialManager, T blockEntity);

	/**
	 * Checks if the given block entity should not be rendered normally.
	 * @param blockEntity The block entity to check.
	 * @return {@code true} if the block entity should not be rendered normally, {@code false} if it should.
	 */
	boolean shouldSkipRender(T blockEntity);
}
