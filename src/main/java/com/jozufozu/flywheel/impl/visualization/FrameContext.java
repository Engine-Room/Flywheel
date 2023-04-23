package com.jozufozu.flywheel.impl.visualization;

import org.joml.FrustumIntersection;

public record FrameContext(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
}
