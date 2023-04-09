package com.jozufozu.flywheel.api.visualization;

import com.jozufozu.flywheel.api.visual.EntityVisual;

import net.minecraft.world.entity.Entity;

/**
 * A visualizer that will be keyed to an entity type.
 * @param <T> The entity type.
 */
public interface EntityVisualizer<T extends Entity> {
	/**
	 * Given an entity and context, constructs a visual for the entity.
	 *
	 * @param ctx    Context for creating a visual.
	 * @param entity The entity to construct a visual for.
	 * @return The visual.
	 */
	EntityVisual<? super T> createVisual(VisualizationContext ctx, T entity);

	/**
	 * Checks if the given entity should not render normally.
	 * @param entity The entity to check.
	 * @return {@code true} if the entity should not render normally, {@code false} if it should.
	 */
	boolean shouldSkipRender(T entity);
}
