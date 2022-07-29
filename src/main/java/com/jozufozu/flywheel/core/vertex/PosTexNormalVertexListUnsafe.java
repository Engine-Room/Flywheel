package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class PosTexNormalVertexListUnsafe extends AbstractVertexList {

	public PosTexNormalVertexListUnsafe(ByteBuffer copyFrom, int vertexCount) {
		super(copyFrom, vertexCount);
	}

	private long ptr(long idx) {
		return base + idx * 23;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(ptr(index));
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 8);
	}

	@Override
	public byte r(int index) {
		return (byte) 0xFF;
	}

	@Override
	public byte g(int index) {
		return (byte) 0xFF;
	}

	@Override
	public byte b(int index) {
		return (byte) 0xFF;
	}

	@Override
	public byte a(int index) {
		return (byte) 0xFF;
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 12);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(ptr(index) + 16);
	}

	@Override
	public int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	public int light(int index) {
		return 0;
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 20));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 21));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr(index) + 22));
	}
}
