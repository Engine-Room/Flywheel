package com.jozufozu.flywheel.core.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class PosTexNormalVertexList extends AbstractVertexList {
	protected static final int STRIDE = 23;

	protected long idxPtr(long idx) {
		return ptr + idx * STRIDE;
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
		return MemoryUtil.memGetFloat(idxPtr(index) + 12);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(idxPtr(index) + 16);
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
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 20));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 21));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + 22));
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
	}

	@Override
	public void g(int index, byte g) {
	}

	@Override
	public void b(int index, byte b) {
	}

	@Override
	public void a(int index, byte a) {
	}

	@Override
	public void u(int index, float u) {
		MemoryUtil.memPutFloat(idxPtr(index) + 12, u);
	}

	@Override
	public void v(int index, float v) {
		MemoryUtil.memPutFloat(idxPtr(index) + 16, v);
	}

	@Override
	public void overlay(int index, int overlay) {
	}

	@Override
	public void light(int index, int light) {
	}

	@Override
	public void normalX(int index, float normalX) {
		MemoryUtil.memPutByte(idxPtr(index) + 20, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		MemoryUtil.memPutByte(idxPtr(index) + 21, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		MemoryUtil.memPutByte(idxPtr(index) + 22, RenderMath.nb(normalZ));
	}

	@Override
	public void shiftPtr(int vertices) {
		ptr += vertices * STRIDE;
	}
}
