package com.jozufozu.flywheel.lib.model.baked;

import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.lib.math.MatrixMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

class TransformingVertexConsumer implements VertexConsumer {
	@UnknownNullability
	private VertexConsumer delegate;
	@UnknownNullability
	private PoseStack poseStack;

	public void prepare(VertexConsumer delegate, PoseStack poseStack) {
		this.delegate = delegate;
		this.poseStack = poseStack;
	}

	public void clear() {
		delegate = null;
		poseStack = null;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		Matrix4f matrix = poseStack.last().pose();
		float fx = (float) x;
		float fy = (float) y;
		float fz = (float) z;
		delegate.vertex(
				MatrixMath.transformPositionX(matrix, fx, fy, fz),
				MatrixMath.transformPositionY(matrix, fx, fy, fz),
				MatrixMath.transformPositionZ(matrix, fx, fy, fz));
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		delegate.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		delegate.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		delegate.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		delegate.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		Matrix3f matrix = poseStack.last().normal();
		float fx = (float) x;
		float fy = (float) y;
		float fz = (float) z;
		delegate.normal(
				MatrixMath.transformNormalX(matrix, fx, fy, fz),
				MatrixMath.transformNormalY(matrix, fx, fy, fz),
				MatrixMath.transformNormalZ(matrix, fx, fy, fz));
		return this;
	}

	@Override
	public void endVertex() {
		delegate.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		delegate.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		delegate.unsetDefaultColor();
	}
}
