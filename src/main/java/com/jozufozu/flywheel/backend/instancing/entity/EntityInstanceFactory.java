package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface EntityInstanceFactory<E extends Entity> {
	EntityInstance<? super E> create(MaterialManager manager, E te);
}
