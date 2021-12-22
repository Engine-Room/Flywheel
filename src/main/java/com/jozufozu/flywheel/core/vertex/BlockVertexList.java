package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.datafixers.util.Pair;

public class BlockVertexList implements VertexList {

	private final ByteBuffer buffer;
	private final int vertexCount;
	private final int stride;

	public BlockVertexList(BufferBuilder builder) {
		Pair<BufferBuilder.DrawState, ByteBuffer> data = builder.popNextBuffer();
		buffer = data.getSecond();

		stride = builder.getVertexFormat()
				.getVertexSize();

		vertexCount = data.getFirst()
				.vertexCount();

	}

	@Override
	public boolean isEmpty() {
		return vertexCount == 0;
	}

	private int vertIdx(int vertexIndex) {
		return vertexIndex * stride;
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

}
