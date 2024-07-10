package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

class MeshEmitter implements VertexConsumer {
	private final RenderType renderType;
	private final BufferBuilder bufferBuilder;

	private BakedModelBufferer.@UnknownNullability ResultConsumer resultConsumer;
	private boolean currentShade;

    MeshEmitter(RenderType renderType) {
        this.renderType = renderType;
		this.bufferBuilder = new BufferBuilder(renderType.bufferSize());
    }

	public void prepare(BakedModelBufferer.ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder.building()) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder unwrap(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	private void prepareForGeometry(boolean shade) {
		if (!bufferBuilder.building()) {
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		} else if (shade != currentShade) {
			emit();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		currentShade = shade;
	}

	private void prepareForGeometry(BakedQuad quad) {
		prepareForGeometry(quad.isShade());
	}

	private void emit() {
		var renderedBuffer = bufferBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer != null) {
			resultConsumer.accept(renderType, currentShade, renderedBuffer);
			renderedBuffer.release();
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay, boolean readExistingColor) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, light, overlay, readExistingColor);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		throw new UnsupportedOperationException("MeshEmitter only supports putBulkData!");
	}
}
