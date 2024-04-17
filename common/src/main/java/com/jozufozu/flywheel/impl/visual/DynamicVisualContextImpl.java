package com.jozufozu.flywheel.impl.visual;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.visual.DistanceUpdateLimiter;
import com.jozufozu.flywheel.api.visual.DynamicVisual;

import net.minecraft.client.Camera;

public record DynamicVisualContextImpl(Camera camera, FrustumIntersection frustum, float partialTick,
									   DistanceUpdateLimiter limiter) implements DynamicVisual.Context {
}
