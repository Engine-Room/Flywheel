package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.api.MaterialManager;

import net.minecraft.world.entity.Entity;

public interface EntityInstancingController<T extends Entity> {
	EntityInstance<? super T> createInstance(MaterialManager materialManager, T entity);

	boolean shouldSkipRender(T entity);
}
