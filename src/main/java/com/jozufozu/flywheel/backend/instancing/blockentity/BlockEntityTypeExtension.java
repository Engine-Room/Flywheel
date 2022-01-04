package com.jozufozu.flywheel.backend.instancing.blockentity;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityTypeExtension<T extends BlockEntity> {
	BlockEntityInstancingController<? super T> flywheel$getInstancingController();

	void flywheel$setInstancingController(BlockEntityInstancingController<? super T> instancingController);
}
