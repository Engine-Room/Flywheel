package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.ShadedVertexList;
import com.jozufozu.flywheel.util.RenderMath;

public class BlockVertexListUnsafe extends AbstractVertexList {

	public BlockVertexListUnsafe(ByteBuffer copyFrom, int vertexCount) {
		super(copyFrom, vertexCount);
	}

	private long ptr(long index) {
		return base + index * 32;
	}

	@Override
	public boolean isEmpty() {
		return vertexCount == 0;
	}

	@Override
	public float getX(int index) {
		return MemoryUtil.memGetFloat(ptr(index));
	}

	@Override
	public float getY(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 4);
	}

	@Override
	public float getZ(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 8);
	}

	@Override
	public byte getR(int index) {
		return MemoryUtil.memGetByte(ptr(index) + 12);
	}

	@Override
	public byte getG(int index) {
		return MemoryUtil.memGetByte(ptr(index) + 13);
	}

	@Override
	public byte getB(int index) {
		return MemoryUtil.memGetByte(ptr(index) + 14);
	}

	@Override
	public byte getA(int index) {
		return MemoryUtil.memGetByte(ptr(index) + 15);
	}

	@Override
	public float getU(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 16);
	}

	@Override
	public float getV(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 20);
	}

	@Override
	public int getLight(int index) {
		return MemoryUtil.memGetInt(ptr(index) + 24);
	}

	@Override
	public float getNX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 28));
	}

	@Override
	public float getNY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 29));
	}

	@Override
	public float getNZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 30));
	}

	public static class Shaded extends BlockVertexListUnsafe implements ShadedVertexList {

		private final int unshadedStartVertex;

		public Shaded(ByteBuffer buffer, int vertexCount, int unshadedStartVertex) {
			super(buffer, vertexCount);
			this.unshadedStartVertex = unshadedStartVertex;
		}

		@Override
		public boolean isShaded(int index) {
			return index < unshadedStartVertex;
		}

	}

}
