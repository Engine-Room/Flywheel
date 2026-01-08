package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@ApiStatus.Internal
public class NeoForgeMeshEmitter extends MeshEmitter {
	private boolean ao;

	NeoForgeMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, ChunkSectionLayer chunkSectionLayer) {
		super(byteBufferBuilderStack, chunkSectionLayer);
	}

	// Called from ModelBlockRendererMixin if AO is on for the model before each part is buffered
	public void prepareForPart(boolean ao) {
		this.ao = ao;
	}

	@Nullable
	private BufferBuilder getBuffer(BakedQuad quad) {
		boolean shade = quad.shade();
		boolean ao = quad.hasAmbientOcclusion() && this.ao;
		return getBuffer(shade, ao);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay) {
		BufferBuilder bufferBuilder = getBuffer(quad);
		if (bufferBuilder != null) {
			bufferBuilder.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay);
		}
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setColor(int color) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		throw new UnsupportedOperationException("NeoForgeMeshEmitter only supports putBulkData!");
	}
}
