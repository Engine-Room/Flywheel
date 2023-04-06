package com.jozufozu.flywheel.api.instance.controller;

import com.jozufozu.flywheel.api.instance.EntityInstance;

import net.minecraft.world.entity.Entity;

/**
 * An instancing controller that will be keyed to an entity type.
 * @param <T> The entity type.
 */
public interface EntityInstancingController<T extends Entity> {
	/**
	 * Given an entity and context, constructs an instance for the entity.
	 *
	 * @param ctx    Context for creating an Instance.
	 * @param entity The entity to construct an instance for.
	 * @return The instance.
	 */
	EntityInstance<? super T> createInstance(InstanceContext ctx, T entity);

	/**
	 * Checks if the given entity should not render normally.
	 * @param entity The entity to check.
	 * @return {@code true} if the entity should not render normally, {@code false} if it should.
	 */
	boolean shouldSkipRender(T entity);
}
