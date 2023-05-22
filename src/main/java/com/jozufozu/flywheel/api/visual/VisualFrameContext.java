package com.jozufozu.flywheel.api.visual;

import org.joml.FrustumIntersection;

public record VisualFrameContext(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum,
								 DistanceUpdateLimiter limiter) {
}
