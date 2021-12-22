package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;

public class BlockVertexListUnsafe implements VertexList {

	private final int vertexCount;
	private final int stride;
	private final long base;

	public BlockVertexListUnsafe(BufferBuilder builder) {
		VertexFormat vertexFormat = builder.getVertexFormat();
		Pair<BufferBuilder.DrawState, ByteBuffer> data = builder.popNextBuffer();
		this.base = MemoryUtil.memAddress(data.getSecond());
		this.vertexCount = data.getFirst().vertexCount();
		this.stride = vertexFormat.getVertexSize();
	}

	private long ptr(long index) {
		return base + index * stride;
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

	@Override
	public int getVertexCount() {
		return vertexCount;
	}
}
