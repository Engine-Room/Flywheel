package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;

class FabricMeshEmitter extends MeshEmitter {
	private boolean shade;
	private boolean ao;

	FabricMeshEmitter(ByteBufferBuilderStack byteBufferBuilderStack, ChunkSectionLayer chunkSectionLayer) {
		super(byteBufferBuilderStack, chunkSectionLayer);
	}

	public void prepareForGeometry(boolean shade, boolean ao) {
		this.shade = shade;
		this.ao = ao;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.addVertex(x, y, z);
		}
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setColor(red, green, blue, alpha);
		}
		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setColor(color);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setUv(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setUv1(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setUv2(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setNormal(x, y, z);
		}
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.setLineWidth(width);
		}
		return this;
	}

	@Override
	public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.addVertex(x, y, z, color, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
		}
	}

	@Override
	public void putBakedQuad(Pose pose, BakedQuad quad, QuadInstance instance) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.putBakedQuad(pose, quad, instance);
		}
	}

	@Override
	public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
		BufferBuilder bufferBuilder = getBuffer(shade, ao);
		if (bufferBuilder != null) {
			bufferBuilder.putBlockBakedQuad(x, y, z, quad, instance);
		}
	}
}
