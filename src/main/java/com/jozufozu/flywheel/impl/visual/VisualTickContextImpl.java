package com.jozufozu.flywheel.impl.visual;

import com.jozufozu.flywheel.api.visual.DistanceUpdateLimiter;
import com.jozufozu.flywheel.api.visual.VisualTickContext;

public record VisualTickContextImpl(double cameraX, double cameraY, double cameraZ,
									DistanceUpdateLimiter limiter) implements VisualTickContext {
}
