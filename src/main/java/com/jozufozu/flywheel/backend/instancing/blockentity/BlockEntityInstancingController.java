package com.jozufozu.flywheel.backend.instancing.blockentity;

import com.jozufozu.flywheel.api.MaterialManager;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityInstancingController<T extends BlockEntity> {
	BlockEntityInstance<? super T> createInstance(MaterialManager materialManager, T blockEntity);

	boolean shouldSkipRender(T blockEntity);
}
