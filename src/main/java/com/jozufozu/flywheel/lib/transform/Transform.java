package com.jozufozu.flywheel.lib.transform;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

public interface Transform<Self extends Transform<Self>> extends Affine<Self> {
	Self mulPose(Matrix4f pose);

	Self mulNormal(Matrix3f normal);

	default Self transform(Matrix4f pose, Matrix3f normal) {
		return mulPose(pose).mulNormal(normal);
	}

	default Self transform(PoseStack.Pose pose) {
		return transform(pose.pose(), pose.normal());
	}

	default Self transform(PoseStack stack) {
		return transform(stack.last());
	}
}
