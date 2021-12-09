package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.backend.api.MaterialManager;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface IEntityInstanceFactory<E extends Entity> {
	EntityInstance<? super E> create(MaterialManager manager, E te);
}
