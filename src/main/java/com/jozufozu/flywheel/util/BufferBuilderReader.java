package com.jozufozu.flywheel.util;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.fabric.helper.BufferBuilderHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;

public class BufferBuilderReader {

	private final ByteBuffer buffer;
	private final int vertexCount;
	private final int formatSize;
	private final int size;

	public BufferBuilderReader(BufferBuilder builder) {
		VertexFormat vertexFormat = BufferBuilderHelper.getVertexFormat(builder);
		Pair<BufferBuilder.DrawState, ByteBuffer> data = builder.popNextBuffer();
		buffer = data.getSecond();
		BufferBuilderHelper.fixByteOrder(builder, buffer);

		formatSize = vertexFormat.getVertexSize();

		vertexCount = data.getFirst()
				.vertexCount();

		size = vertexCount * formatSize;

		// TODO: adjust the getters based on the input format
		//		ImmutableList<VertexFormatElement> elements = vertexFormat.getElements();
		//		for (int i = 0, size = elements.size(); i < size; i++) {
		//			VertexFormatElement element = elements.get(i);
		//			int offset = vertexFormat.getOffset(i);
		//
		//			element.getUsage()
		//		}
	}

	public boolean isEmpty() {
		return vertexCount == 0;
	}

	public int vertIdx(int vertexIndex) {
		return vertexIndex * formatSize;
	}

	public float getX(int index) {
		return buffer.getFloat(vertIdx(index));
	}

	public float getY(int index) {
		return buffer.getFloat(vertIdx(index) + 4);
	}

	public float getZ(int index) {
		return buffer.getFloat(vertIdx(index) + 8);
	}

	public byte getR(int index) {
		return buffer.get(vertIdx(index) + 12);
	}

	public byte getG(int index) {
		return buffer.get(vertIdx(index) + 13);
	}

	public byte getB(int index) {
		return buffer.get(vertIdx(index) + 14);
	}

	public byte getA(int index) {
		return buffer.get(vertIdx(index) + 15);
	}

	public float getU(int index) {
		return buffer.getFloat(vertIdx(index) + 16);
	}

	public float getV(int index) {
		return buffer.getFloat(vertIdx(index) + 20);
	}

	public int getLight(int index) {
		return buffer.getInt(vertIdx(index) + 24);
	}

	public byte getNX(int index) {
		return buffer.get(vertIdx(index) + 28);
	}

	public byte getNY(int index) {
		return buffer.get(vertIdx(index) + 29);
	}

	public byte getNZ(int index) {
		return buffer.get(vertIdx(index) + 30);
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public int getSize() {
		return size;
	}
}
