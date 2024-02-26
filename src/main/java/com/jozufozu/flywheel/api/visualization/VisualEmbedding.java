package com.jozufozu.flywheel.api.visualization;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;

import net.minecraft.world.phys.AABB;

public interface VisualEmbedding {
	Matrix4fc pose();

	Matrix3fc normal();

	AABB boundingBox();
}
