package com.jozufozu.flywheel.backend.instancing.entity;

import net.minecraft.world.entity.Entity;

public interface EntityTypeExtension<T extends Entity> {
	EntityInstancingController<? super T> flywheel$getInstancingController();

	void flywheel$setInstancingController(EntityInstancingController<? super T> instancingController);
}
