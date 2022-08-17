package com.jozufozu.flywheel.extension;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.instancing.entity.EntityInstancingController;

import net.minecraft.world.entity.Entity;

public interface EntityTypeExtension<T extends Entity> {
	@Nullable
	EntityInstancingController<? super T> flywheel$getInstancingController();

	void flywheel$setInstancingController(@Nullable EntityInstancingController<? super T> instancingController);
}
