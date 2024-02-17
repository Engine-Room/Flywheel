package com.jozufozu.flywheel.api.visual;

import org.joml.FrustumIntersection;

import net.minecraft.client.Camera;

public interface VisualFrameContext {
	Camera camera();

	FrustumIntersection frustum();

	float partialTick();

	DistanceUpdateLimiter limiter();
}
