package com.jozufozu.flywheel.lib.model.buffering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface ShadeSeparatingVertexConsumer<T extends VertexConsumer> extends VertexConsumer {
	T getShadedConsumer();

	T getUnshadedConsumer();

	@Override
	default void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
		if (quad.isShade()) {
			getShadedConsumer().putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		} else {
			getUnshadedConsumer().putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		}
	}

	@Override
	default void putBulkData(PoseStack.Pose matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
		if (bakedQuad.isShade()) {
			getShadedConsumer().putBulkData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
		} else {
			getUnshadedConsumer().putBulkData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
		}
	}

	@Override
	default VertexConsumer vertex(double x, double y, double z) {
		return getUnshadedConsumer().vertex(x, y, z);
	}

	@Override
	default VertexConsumer color(int red, int green, int blue, int alpha) {
		return getUnshadedConsumer().color(red, green, blue, alpha);
	}

	@Override
	default VertexConsumer uv(float u, float v) {
		return getUnshadedConsumer().uv(u, v);
	}

	@Override
	default VertexConsumer overlayCoords(int u, int v) {
		return getUnshadedConsumer().overlayCoords(u, v);
	}

	@Override
	default VertexConsumer uv2(int u, int v) {
		return getUnshadedConsumer().uv2(u, v);
	}

	@Override
	default VertexConsumer normal(float x, float y, float z) {
		return getUnshadedConsumer().normal(x, y, z);
	}

	@Override
	default void endVertex() {
		getUnshadedConsumer().endVertex();
	}

	@Override
	default void defaultColor(int red, int green, int blue, int alpha) {
		getUnshadedConsumer().defaultColor(red, green, blue, alpha);
	}

	@Override
	default void unsetDefaultColor() {
		getUnshadedConsumer().unsetDefaultColor();
	}
}
