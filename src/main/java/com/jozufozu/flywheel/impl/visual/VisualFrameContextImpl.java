package com.jozufozu.flywheel.impl.visual;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.visual.DistanceUpdateLimiter;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;

import net.minecraft.client.Camera;

public record VisualFrameContextImpl(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum,
									 float partialTick, DistanceUpdateLimiter limiter,
									 Camera camera) implements VisualFrameContext {
}
