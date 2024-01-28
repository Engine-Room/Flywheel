package com.jozufozu.flywheel.api.visual;

import org.jetbrains.annotations.ApiStatus;
import org.joml.FrustumIntersection;

import net.minecraft.client.Camera;

@ApiStatus.NonExtendable
public interface VisualFrameContext {
	Camera camera();

	FrustumIntersection frustum();

	float partialTick();

	DistanceUpdateLimiter limiter();
}
