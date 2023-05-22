package com.jozufozu.flywheel.api.visual;

public record VisualTickContext(double cameraX, double cameraY, double cameraZ, DistanceUpdateLimiter limiter) {
}
