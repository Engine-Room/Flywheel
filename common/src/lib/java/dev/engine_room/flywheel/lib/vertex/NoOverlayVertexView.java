package dev.engine_room.flywheel.lib.vertex;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.lib.math.RenderMath;

public class NoOverlayVertexView extends AbstractVertexView implements EmptyVertexList {
	public static final long STRIDE = 31;

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
	public float r(int index) {
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * STRIDE + 12));
	}

	@Override
	public float g(int index) {
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * STRIDE + 13));
	}

	@Override
	public float b(int index) {
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * STRIDE + 14));
	}

	@Override
	public float a(int index) {
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * STRIDE + 15));
	}

	@Override
	public float u(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 16);
	}

	@Override
	public float v(int index) {
		return MemoryUtil.memGetFloat(ptr + index * STRIDE + 20);
	}

	@Override
	public int light(int index) {
		return MemoryUtil.memGetInt(ptr + index * STRIDE + 24);
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 28));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 29));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * STRIDE + 30));
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
	public void r(int index, float r) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 12, RenderMath.unb(r));
	}

	@Override
	public void g(int index, float g) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 13, RenderMath.unb(g));
	}

	@Override
	public void b(int index, float b) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 14, RenderMath.unb(b));
	}

	@Override
	public void a(int index, float a) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 15, RenderMath.unb(a));
	}

	@Override
	public void u(int index, float u) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 16, u);
	}

	@Override
	public void v(int index, float v) {
		MemoryUtil.memPutFloat(ptr + index * STRIDE + 20, v);
	}

	@Override
	public void light(int index, int light) {
		MemoryUtil.memPutInt(ptr + index * STRIDE + 24, light);
	}

	@Override
	public void normalX(int index, float normalX) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 28, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 29, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		MemoryUtil.memPutByte(ptr + index * STRIDE + 30, RenderMath.nb(normalZ));
	}
}
