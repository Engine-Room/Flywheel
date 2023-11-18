package com.jozufozu.flywheel.api.visual;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

// TODO: Consider adding LevelAccessor getter
public interface Effect {
	EffectVisual<?> visualize(VisualizationContext ctx);
}
