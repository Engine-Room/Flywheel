package com.jozufozu.flywheel.core.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class BlockVertexList extends AbstractVertexList {
	protected static final int STRIDE = 32;

	protected long idxPtr(int index) {
		return ptr + index * STRIDE;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index));
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index) + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index) + 8);
	}

	@Override
	public byte r(int index) {
		return MemoryUtil.memGetByte(idxPtr(index) + 12);
	}

	@Override
	public byte g(int index) {
		return MemoryUtil.memGetByte(idxPtr(index) + 13);
	}

	@Override
	public byte b(int index) {
		return MemoryUtil.memGetByte(idxPtr(index) + 14);
	}

	@Override
	public byte a(int index) {
		return MemoryUtil.memGetByte(idxPtr(index) + 15);
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index) + 16);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index) + 20);
	}

	@Override
	public int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	public int light(int index) {
		return MemoryUtil.memGetInt(idxPtr(index) + 24) << 4;
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 28));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 29));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 30));
	}

	@Override
	public void x(int index, float x) {
		MemoryUtil.memPutFloat(idxPtr(index), x);
	}

	@Override
	public void y(int index, float y) {
		MemoryUtil.memPutFloat(idxPtr(index) + 4, y);
	}

	@Override
	public void z(int index, float z) {
		MemoryUtil.memPutFloat(idxPtr(index) + 8, z);
	}

	@Override
	public void r(int index, byte r) {
		MemoryUtil.memPutByte(idxPtr(index) + 12, r);
	}

	@Override
	public void g(int index, byte g) {
		MemoryUtil.memPutByte(idxPtr(index) + 13, g);
	}

	@Override
	public void b(int index, byte b) {
		MemoryUtil.memPutByte(idxPtr(index) + 14, b);
	}

	@Override
	public void a(int index, byte a) {
		MemoryUtil.memPutByte(idxPtr(index) + 15, a);
	}

	@Override
	public void u(int index, float u) {
		MemoryUtil.memPutFloat(idxPtr(index) + 16, u);
	}

	@Override
	public void v(int index, float v) {
		MemoryUtil.memPutFloat(idxPtr(index) + 20, v);
	}

	@Override
	public void overlay(int index, int overlay) {
	}

	@Override
	public void light(int index, int light) {
		MemoryUtil.memPutInt(idxPtr(index) + 24, light >> 4);
	}

	@Override
	public void normalX(int index, float normalX) {
		MemoryUtil.memPutByte(idxPtr(index) + 28, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		MemoryUtil.memPutByte(idxPtr(index) + 29, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		MemoryUtil.memPutByte(idxPtr(index) + 30, RenderMath.nb(normalZ));
	}

	@Override
	public int vertexStride() {
		return STRIDE;
	}
}
