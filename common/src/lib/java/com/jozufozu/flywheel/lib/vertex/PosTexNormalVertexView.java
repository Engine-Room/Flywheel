package com.jozufozu.flywheel.lib.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.math.RenderMath;

public class PosTexNormalVertexView extends AbstractVertexView implements EmptyVertexList {
	public static final long STRIDE = 23;

	@Override
	public long stride() {
		return STRIDE;
	}

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE);
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 8);
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 12);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 16);
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 20));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 21));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 22));
	}

	@Override
	public void x(int index, float x) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE, x);
	}

	@Override
	public void y(int index, float y) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 4, y);
	}

	@Override
	public void z(int index, float z) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 8, z);
	}

	@Override
	public void u(int index, float u) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 12, u);
	}

	@Override
	public void v(int index, float v) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 16, v);
	}

	@Override
	public void normalX(int index, float normalX) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 20, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 21, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 22, RenderMath.nb(normalZ));
	}
}
