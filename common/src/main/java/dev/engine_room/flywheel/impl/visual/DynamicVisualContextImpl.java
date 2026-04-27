package dev.engine_room.flywheel.impl.visual;

import org.joml.FrustumIntersection;

import dev.engine_room.flywheel.api.visual.DistanceUpdateLimiter;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import net.minecraft.client.Camera;

public record DynamicVisualContextImpl(Camera camera, FrustumIntersection frustum, float partialTick,
									   DistanceUpdateLimiter limiter) implements DynamicVisual.Context {
}
