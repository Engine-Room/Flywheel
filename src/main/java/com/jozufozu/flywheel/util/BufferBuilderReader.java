package com.jozufozu.flywheel.util;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;

public class BufferBuilderReader implements ModelReader {

	private final ByteBuffer buffer;
	private final int vertexCount;
	private final int formatSize;
	private final int size;

	public BufferBuilderReader(BufferBuilder builder) {
		VertexFormat vertexFormat = builder.getVertexFormat();
		Pair<BufferBuilder.DrawState, ByteBuffer> data = builder.popNextBuffer();
		buffer = data.getSecond();

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

	@Override
	public boolean isEmpty() {
		return vertexCount == 0;
	}

	private int vertIdx(int vertexIndex) {
		return vertexIndex * formatSize;
	}

	@Override
	public float getX(int index) {
		return buffer.getFloat(vertIdx(index));
	}

	@Override
	public float getY(int index) {
		return buffer.getFloat(vertIdx(index) + 4);
	}

	@Override
	public float getZ(int index) {
		return buffer.getFloat(vertIdx(index) + 8);
	}

	@Override
	public byte getR(int index) {
		return buffer.get(vertIdx(index) + 12);
	}

	@Override
	public byte getG(int index) {
		return buffer.get(vertIdx(index) + 13);
	}

	@Override
	public byte getB(int index) {
		return buffer.get(vertIdx(index) + 14);
	}

	@Override
	public byte getA(int index) {
		return buffer.get(vertIdx(index) + 15);
	}

	@Override
	public float getU(int index) {
		return buffer.getFloat(vertIdx(index) + 16);
	}

	@Override
	public float getV(int index) {
		return buffer.getFloat(vertIdx(index) + 20);
	}

	@Override
	public int getLight(int index) {
		return buffer.getInt(vertIdx(index) + 24);
	}

	@Override
	public float getNX(int index) {
		return RenderMath.f(buffer.get(vertIdx(index) + 28));
	}

	@Override
	public float getNY(int index) {
		return RenderMath.f(buffer.get(vertIdx(index) + 29));
	}

	@Override
	public float getNZ(int index) {
		return RenderMath.f(buffer.get(vertIdx(index) + 30));
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	public int getSize() {
		return size;
	}
}
