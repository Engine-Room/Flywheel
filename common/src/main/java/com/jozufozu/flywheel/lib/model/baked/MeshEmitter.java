package com.jozufozu.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

class MeshEmitter implements VertexConsumer {
	private final BufferBuilder bufferBuilder;
	private final RenderType renderType;
	private boolean lastQuadWasShaded;
	private boolean seenFirstQuad;
	@Nullable
	private BakedModelBufferer.ResultConsumer resultConsumer;

    MeshEmitter(BufferBuilder bufferBuilder, RenderType renderType) {
        this.bufferBuilder = bufferBuilder;
        this.renderType = renderType;
    }

	public void begin(BakedModelBufferer.ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;

		begin();
	}

	public void end() {
		emit();
		seenFirstQuad = false;
		resultConsumer = null;
	}

	private void begin() {
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
	}

	private void emit() {
		var renderedBuffer = bufferBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer != null) {
			if (resultConsumer != null) {
				resultConsumer.accept(renderType, lastQuadWasShaded, renderedBuffer);
			}
			renderedBuffer.release();
		}
	}

	private void observeQuadAndEmitIfNecessary(BakedQuad quad) {
		if (seenFirstQuad && lastQuadWasShaded != quad.isShade()) {
			emit();
			begin();
		}

		seenFirstQuad = true;
		lastQuadWasShaded = quad.isShade();
	}

	@Override
	public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
		observeQuadAndEmitIfNecessary(quad);

		bufferBuilder.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
	}

	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
		observeQuadAndEmitIfNecessary(quad);

		bufferBuilder.putBulkData(matrixEntry, quad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void endVertex() {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void unsetDefaultColor() {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}
}
