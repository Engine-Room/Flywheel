package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;

public record RenderContext(ClientLevel level, PoseStack stack, Matrix4f viewProjection, RenderBuffers buffers,
							double camX, double camY, double camZ) implements TransformStack {

	public static RenderContext CURRENT;

	@Override
	public TransformStack multiply(Quaternion quaternion) {
		return TransformStack.cast(stack).multiply(quaternion);
	}

	@Override
	public TransformStack scale(float factorX, float factorY, float factorZ) {
		return TransformStack.cast(stack).scale(factorX, factorY, factorZ);
	}

	@Override
	public TransformStack pushPose() {
		stack.pushPose();
		return TransformStack.cast(stack);
	}

	@Override
	public TransformStack popPose() {
		stack.popPose();
		return TransformStack.cast(stack);
	}

	@Override
	public TransformStack mulPose(Matrix4f pose) {
		return TransformStack.cast(stack).mulPose(pose);
	}

	@Override
	public TransformStack mulNormal(Matrix3f normal) {
		return TransformStack.cast(stack).mulNormal(normal);
	}

	@Override
	public TransformStack translate(double x, double y, double z) {
		return TransformStack.cast(stack).translate(x, y, z);
	}
}
