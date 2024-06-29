package dev.engine_room.flywheel.api.visualization;

import java.util.List;

import dev.engine_room.flywheel.api.visual.EntityVisual;
import net.minecraft.client.renderer.entity.EntityRenderer;
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
	List<EntityVisual<? super T>> createVisual(VisualizationContext ctx, T entity);

	/**
	 * Checks if the given entity should not render with the vanilla {@link EntityRenderer}.
	 * @param entity The entity to check.
	 * @return {@code true} if the entity should not render with the vanilla {@link EntityRenderer}, {@code false} if it should.
	 */
	boolean skipVanillaRender(T entity);
}
