package com.jozufozu.flywheel.backend.instancing.entity;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;

public interface EntityTypeExtension<T extends Entity> {
	@Nullable
	EntityInstancingController<? super T> flywheel$getInstancingController();

	void flywheel$setInstancingController(@Nullable EntityInstancingController<? super T> instancingController);
}
