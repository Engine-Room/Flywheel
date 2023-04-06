package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.controller.BlockEntityInstancingController;
import com.jozufozu.flywheel.api.instance.controller.EntityInstancingController;
import com.jozufozu.flywheel.extension.BlockEntityTypeExtension;
import com.jozufozu.flywheel.extension.EntityTypeExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

//TODO: Add freezing
@SuppressWarnings("unchecked")
public final class InstancingControllerRegistryImpl {
	@Nullable
	public static <T extends BlockEntity> BlockEntityInstancingController<? super T> getController(BlockEntityType<T> type) {
		return ((BlockEntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	@Nullable
	public static <T extends Entity> EntityInstancingController<? super T> getController(EntityType<T> type) {
		return ((EntityTypeExtension<T>) type).flywheel$getInstancingController();
	}

	public static <T extends BlockEntity> void setController(BlockEntityType<T> type, BlockEntityInstancingController<? super T> instancingController) {
		((BlockEntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	public static <T extends Entity> void setController(EntityType<T> type, EntityInstancingController<? super T> instancingController) {
		((EntityTypeExtension<T>) type).flywheel$setInstancingController(instancingController);
	}

	private InstancingControllerRegistryImpl() {
	}
}
