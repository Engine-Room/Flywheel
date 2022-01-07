package com.jozufozu.flywheel.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityTypeExtension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeExtension<T> {
	@Unique
	private BlockEntityInstancingController<? super T> flywheel$instancingController;

	@Override
	@Nullable
	public BlockEntityInstancingController<? super T> flywheel$getInstancingController() {
		return flywheel$instancingController;
	}

	@Override
	public void flywheel$setInstancingController(@Nullable BlockEntityInstancingController<? super T> instancingController) {
		this.flywheel$instancingController = instancingController;
	}
}
