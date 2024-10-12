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
	private boolean filledPosition;
	private boolean filledTexture;
	private boolean filledNormal;

	public VertexWriter() {
		data = MemoryBlock.malloc(128 * STRIDE);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		if (!filledPosition) {
			long ptr = vertexPtr();
			MemoryUtil.memPutFloat(ptr, x);
			MemoryUtil.memPutFloat(ptr + 4, y);
			MemoryUtil.memPutFloat(ptr + 8, z);
			filledPosition = true;
		}
		return endVertexIfNeeded();
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		return endVertexIfNeeded();
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (!filledTexture) {
			long ptr = vertexPtr();
			MemoryUtil.memPutFloat(ptr + 12, u);
			MemoryUtil.memPutFloat(ptr + 16, v);
			filledTexture = true;
		}
		return endVertexIfNeeded();
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		// ignore overlay
		return endVertexIfNeeded();
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		// ignore light
		return endVertexIfNeeded();
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
		return endVertexIfNeeded();
	}

	public VertexConsumer endVertexIfNeeded() {
		if (!filledPosition || !filledTexture || !filledNormal) {
			// We do not throw here as that matched what vanilla does
			return this;
		}

		filledPosition = false;
		filledTexture = false;
		filledNormal = false;
		vertexCount++;

		long byteSize = (vertexCount + 1) * STRIDE;
		long capacity = data.size();
		if (byteSize > capacity) {
			data = data.realloc(capacity * 2);
		}

		return this;
	}

	private long vertexPtr() {
		return data.ptr() + vertexCount * STRIDE;
	}

	public MemoryBlock copyDataAndReset() {
		MemoryBlock dataCopy = MemoryBlock.mallocTracked(vertexCount * STRIDE);
		data.copyTo(dataCopy);

		vertexCount = 0;
		filledPosition = false;
		filledTexture = false;
		filledNormal = false;

		return dataCopy;
	}
}
