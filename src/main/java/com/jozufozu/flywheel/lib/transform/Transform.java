package com.jozufozu.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

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

	default Self rotateCentered(Direction axis, float radians) {
		translate(.5f, .5f, .5f).rotate(axis, radians)
				.translate(-.5f, -.5f, -.5f);
		return (Self) this;
	}

	default Self rotateCentered(Quaternion q) {
		translate(.5f, .5f, .5f).multiply(q)
				.translate(-.5f, -.5f, -.5f);
		return (Self) this;
	}
}
