package com.jozufozu.flywheel.api.visual;

import java.util.Collection;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

// TODO: add level getter?
// TODO: return single visual instead of many?
public interface Effect {
	Collection<EffectVisual<?>> createVisuals(VisualizationContext ctx);
}
