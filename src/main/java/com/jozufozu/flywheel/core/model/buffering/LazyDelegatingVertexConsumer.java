package com.jozufozu.flywheel.core.model.buffering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface LazyDelegatingVertexConsumer<T extends VertexConsumer> extends VertexConsumer {
	T getDelegate();

	@Override
	default VertexConsumer vertex(double x, double y, double z) {
		return getDelegate().vertex(x, y, z);
	}

	@Override
	default VertexConsumer color(int red, int green, int blue, int alpha) {
		return getDelegate().color(red, green, blue, alpha);
	}

	@Override
	default VertexConsumer uv(float u, float v) {
		return getDelegate().uv(u, v);
	}

	@Override
	default VertexConsumer overlayCoords(int u, int v) {
		return getDelegate().overlayCoords(u, v);
	}

	@Override
	default VertexConsumer uv2(int u, int v) {
		return getDelegate().uv2(u, v);
	}

	@Override
	default VertexConsumer normal(float x, float y, float z) {
		return getDelegate().normal(x, y, z);
	}

	@Override
	default void endVertex() {
		getDelegate().endVertex();
	}

	@Override
	default void defaultColor(int red, int green, int blue, int alpha) {
		getDelegate().defaultColor(red, green, blue, alpha);
	}

	@Override
	default void unsetDefaultColor() {
		getDelegate().unsetDefaultColor();
	}

	@Override
	default void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
		getDelegate().putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
	}

	@Override
	default void putBulkData(PoseStack.Pose matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
		getDelegate().putBulkData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
	}
}
