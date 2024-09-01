package com.jozufozu.flywheel.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstancingController;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityTypeExtension;
import com.jozufozu.flywheel.compat.CompatHelper;
import com.jozufozu.flywheel.compat.SodiumCompat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeExtension<T> {
	@Unique
	private BlockEntityInstancingController<? super T> flywheel$instancingController;

	@Unique
	private Object flywheel$sodiumPredicate;

	@Override
	@Nullable
	public BlockEntityInstancingController<? super T> flywheel$getInstancingController() {
		return flywheel$instancingController;
	}

	@Override
	public void flywheel$setInstancingController(@Nullable BlockEntityInstancingController<? super T> instancingController) {
		if (CompatHelper.IS_SODIUM_LOADED.get() && !CompatHelper.IS_EMBEDDIUM_LOADED.get()) {
			if (flywheel$instancingController == null && instancingController != null) {
				flywheel$sodiumPredicate = SodiumCompat.forBlockEntityType((BlockEntityType<?>) (Object) this);
			} else if (flywheel$instancingController != null && instancingController == null) {
				SodiumCompat.removePredicate((BlockEntityType<?>) (Object) this, flywheel$sodiumPredicate);
			}
		}
		this.flywheel$instancingController = instancingController;
	}
}
