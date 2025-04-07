package dev.engine_room.vanillin.compose;

import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public interface ConfiguredElement<T> {
	Visual create(VisualizationContext ctx, T entity, float partialTick);

	boolean shouldVisualize(VisualizationContext ctx, T entity);
}
