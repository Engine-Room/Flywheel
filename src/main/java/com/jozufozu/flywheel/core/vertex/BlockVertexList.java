package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.ShadedVertexList;
import com.jozufozu.flywheel.util.RenderMath;

public class BlockVertexList extends AbstractVertexList {

	private final int stride;

	public BlockVertexList(ByteBuffer copyFrom, int vertexCount, int stride) {
		super(copyFrom, vertexCount);
		this.stride = stride;
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
		return contents.getFloat(vertIdx(index));
	}

	@Override
	public float getY(int index) {
		return contents.getFloat(vertIdx(index) + 4);
	}

	@Override
	public float getZ(int index) {
		return contents.getFloat(vertIdx(index) + 8);
	}

	@Override
	public byte getR(int index) {
		return contents.get(vertIdx(index) + 12);
	}

	@Override
	public byte getG(int index) {
		return contents.get(vertIdx(index) + 13);
	}

	@Override
	public byte getB(int index) {
		return contents.get(vertIdx(index) + 14);
	}

	@Override
	public byte getA(int index) {
		return contents.get(vertIdx(index) + 15);
	}

	@Override
	public float getU(int index) {
		return contents.getFloat(vertIdx(index) + 16);
	}

	@Override
	public float getV(int index) {
		return contents.getFloat(vertIdx(index) + 20);
	}

	@Override
	public int getLight(int index) {
		return contents.getInt(vertIdx(index) + 24);
	}

	@Override
	public float getNX(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 28));
	}

	@Override
	public float getNY(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 29));
	}

	@Override
	public float getNZ(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 30));
	}

	public static class Shaded extends BlockVertexList implements ShadedVertexList {

		private final int unshadedStartVertex;

		public Shaded(ByteBuffer copyFrom, int vertexCount, int stride, int unshadedStartVertex) {
			super(copyFrom, vertexCount, stride);
			this.unshadedStartVertex = unshadedStartVertex;
		}

		@Override
		public boolean isShaded(int index) {
			return index < unshadedStartVertex;
		}

	}

}
