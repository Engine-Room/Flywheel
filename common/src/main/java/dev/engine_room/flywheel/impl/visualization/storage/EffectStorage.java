package dev.engine_room.flywheel.impl.visualization.storage;

import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class EffectStorage extends Storage<Effect> {
	@Override
	protected EffectVisual<?> createRaw(VisualizationContext visualizationContext, Effect obj, float partialTick) {
		return obj.visualize(visualizationContext, partialTick);
	}

	@Override
	public boolean willAccept(Effect obj) {
		return true;
	}
}
