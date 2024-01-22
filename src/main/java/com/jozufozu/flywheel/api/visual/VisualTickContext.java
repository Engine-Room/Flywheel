package com.jozufozu.flywheel.api.visual;

public interface VisualTickContext {
	double cameraX();

	double cameraY();

	double cameraZ();

	DistanceUpdateLimiter limiter();
}
