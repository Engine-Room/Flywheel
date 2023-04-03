package com.jozufozu.flywheel.extension;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.blockentity.BlockEntityInstancingController;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityTypeExtension<T extends BlockEntity> {
	@Nullable
	BlockEntityInstancingController<? super T> flywheel$getInstancingController();

	void flywheel$setInstancingController(@Nullable BlockEntityInstancingController<? super T> instancingController);
}
