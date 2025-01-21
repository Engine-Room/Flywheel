package dev.engine_room.flywheel.lib.model.part;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.math.DataPacker;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.vertex.PosTexNormalVertexView;

class VertexWriter implements VertexConsumer {
	private static final int STRIDE = (int) PosTexNormalVertexView.STRIDE;

	private MemoryBlock data;

	private int vertexCount;
	private boolean filledTexture = true;
	private boolean filledNormal = true;

	public VertexWriter() {
		data = MemoryBlock.malloc(128 * STRIDE);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		endLastVertex();
		vertexCount++;

		long byteSize = vertexCount * STRIDE;
		long capacity = data.size();
		if (byteSize > capacity) {
			data = data.realloc(capacity * 2);
		}

		filledTexture = false;
		filledNormal = false;

		long ptr = vertexPtr();
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		// ignore color
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (!filledTexture) {
			long ptr = vertexPtr();
			MemoryUtil.memPutFloat(ptr + 12, u);
			MemoryUtil.memPutFloat(ptr + 16, v);
			filledTexture = true;
		}
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		// ignore overlay
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		// ignore light
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		if (!filledNormal) {
			long ptr = vertexPtr();
			MemoryUtil.memPutByte(ptr + 20, DataPacker.packNormI8(x));
			MemoryUtil.memPutByte(ptr + 21, DataPacker.packNormI8(y));
			MemoryUtil.memPutByte(ptr + 22, DataPacker.packNormI8(z));
			filledNormal = true;
		}
		return this;
	}

	private long vertexPtr() {
		return data.ptr() + (vertexCount - 1) * STRIDE;
	}

	private void endLastVertex() {
		if (vertexCount != 0) {
			if (!filledTexture || !filledNormal) {
				throw new IllegalStateException("Missing elements in vertex");
			}
		}
	}

	public MemoryBlock copyDataAndReset() {
		endLastVertex();

		MemoryBlock dataCopy = MemoryBlock.mallocTracked(vertexCount * STRIDE);
		data.copyTo(dataCopy);

		vertexCount = 0;
		filledTexture = true;
		filledNormal = true;

		return dataCopy;
	}
}
