package dev.engine_room.vanillin.compose;

import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public interface VisualElement<T, C> {
	Visual create(VisualizationContext ctx, T entity, float partialTick, C config);

	interface Unit<T> extends VisualElement<T, Object> {
		@Override
		default Visual create(VisualizationContext ctx, T entity, float partialTick, Object config) {
			return create(ctx, entity, partialTick);
		}

		Visual create(VisualizationContext ctx, T entity, float partialTick);
	}
}
