package com.jozufozu.flywheel.core.vertex;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.platform.MemoryTracker;

public class PosTexNormalVertexListUnsafe implements VertexList {

	private final ByteBuffer contents;
	private final int vertexCount;
	private final long base;

	public PosTexNormalVertexListUnsafe(ByteBuffer copyFrom, int vertexCount) {
		this.contents = MemoryTracker.create(copyFrom.capacity());
		this.contents.order(copyFrom.order());
		this.contents.put(copyFrom);
		((Buffer) this.contents).flip();

		this.base = MemoryUtil.memAddress(this.contents);
		this.vertexCount = vertexCount;
	}

	private long ptr(long idx) {
		return base + idx * 23;
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
		return (byte) 0xFF;
	}

	@Override
	public byte getG(int index) {
		return (byte) 0xFF;
	}

	@Override
	public byte getB(int index) {
		return (byte) 0xFF;
	}

	@Override
	public byte getA(int index) {
		return (byte) 0xFF;
	}

	@Override
	public float getU(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 12);
	}

	@Override
	public float getV(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 16);
	}

	@Override
	public int getLight(int index) {
		return 0;
	}

	@Override
	public float getNX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 20));
	}

	@Override
	public float getNY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 21));
	}

	@Override
	public float getNZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 22));
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}
}
