package com.jozufozu.flywheel.impl.visual;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.visual.DistanceUpdateLimiter;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;

public record VisualFrameContextImpl(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum,
									 float partialTick, DistanceUpdateLimiter limiter) implements VisualFrameContext {
}
