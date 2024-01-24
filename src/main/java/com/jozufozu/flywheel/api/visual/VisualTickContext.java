package com.jozufozu.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface VisualTickContext {
	double cameraX();

	double cameraY();

	double cameraZ();

	DistanceUpdateLimiter limiter();
}
