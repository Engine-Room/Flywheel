package com.jozufozu.flywheel.api.visual;

import org.joml.FrustumIntersection;

public interface VisualFrameContext {
	double cameraX();

	double cameraY();

	double cameraZ();

	FrustumIntersection frustum();

	float partialTick();

	DistanceUpdateLimiter limiter();
}
