package com.jozufozu.flywheel.api.visual;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

public interface Effect {
	EffectVisual<?> visualize(VisualizationContext ctx);
}
