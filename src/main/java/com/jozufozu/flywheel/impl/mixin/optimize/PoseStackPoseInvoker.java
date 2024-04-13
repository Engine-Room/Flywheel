package com.jozufozu.flywheel.impl.mixin.optimize;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PoseStack.Pose.class)
public interface PoseStackPoseInvoker {
	@Invoker("<init>")
	static PoseStack.Pose flywheel$create(Matrix4f pose, Matrix3f normal) {
		throw new IllegalStateException();
	}
}
