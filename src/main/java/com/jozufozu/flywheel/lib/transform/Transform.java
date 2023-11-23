package com.jozufozu.flywheel.lib.transform;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;

public interface Transform<Self extends Transform<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
	Self mulPose(Matrix4f pose);

	Self mulNormal(Matrix3f normal);

	default Self transform(Matrix4f pose, Matrix3f normal) {
		mulPose(pose);
		return mulNormal(normal);
	}

	default Self transform(PoseStack stack) {
		PoseStack.Pose last = stack.last();
		return transform(last.pose(), last.normal());
	}

	@SuppressWarnings("unchecked")
	default Self rotateCentered(Direction axis, float radians) {
		translate(.5f, .5f, .5f).rotate(axis, radians)
				.translate(-.5f, -.5f, -.5f);
		return (Self) this;
	}

	@SuppressWarnings("unchecked")
	default Self rotateCentered(Quaternionf q) {
		translate(.5f, .5f, .5f).multiply(q)
				.translate(-.5f, -.5f, -.5f);
		return (Self) this;
	}
}
