package com.jozufozu.flywheel.api.instance.controller;

import com.jozufozu.flywheel.api.instance.BlockEntityInstance;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * An instancing controller that will be keyed to a block entity type.
 * @param <T> The block entity type.
 */
public interface BlockEntityInstancingController<T extends BlockEntity> {
	/**
	 * Given a block entity and context, constructs an instance for the block entity.
	 *
	 * @param ctx         Context for creating an Instance.
	 * @param blockEntity The block entity to construct an instance for.
	 * @return The instance.
	 */
	BlockEntityInstance<? super T> createInstance(InstanceContext ctx, T blockEntity);

	/**
	 * Checks if the given block entity should not be rendered normally.
	 * @param blockEntity The block entity to check.
	 * @return {@code true} if the block entity should not be rendered normally, {@code false} if it should.
	 */
	boolean shouldSkipRender(T blockEntity);
}
