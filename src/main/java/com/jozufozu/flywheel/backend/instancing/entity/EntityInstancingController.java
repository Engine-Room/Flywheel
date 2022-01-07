package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.api.MaterialManager;

import net.minecraft.world.entity.Entity;

/**
 * An instancing controller that will be keyed to an entity type.
 * @param <T> The entity type.
 */
public interface EntityInstancingController<T extends Entity> {
	/**
	 * Given an entity and a material manager, constructs an instance for the entity.
	 * @param materialManager The material manager to use.
	 * @param entity The entity to construct an instance for.
	 * @return The instance.
	 */
	EntityInstance<? super T> createInstance(MaterialManager materialManager, T entity);

	/**
	 * Checks if the given entity should not render normally.
	 * @param entity The entity to check.
	 * @return {@code true} if the entity should not render normally, {@code false} if it should.
	 */
	boolean shouldSkipRender(T entity);
}
