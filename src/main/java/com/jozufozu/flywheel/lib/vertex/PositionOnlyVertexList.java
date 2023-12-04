package com.jozufozu.flywheel.lib.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PositionOnlyVertexList extends AbstractVertexList {
	public static final int STRIDE = 12;

	@Override
	public float x(int index) {
		return MemoryUtil.memGetFloat(ptr + (long) index * STRIDE);
	}

	@Override
	public float y(int index) {
		return MemoryUtil.memGetFloat(ptr + (long) index * STRIDE + 4);
	}

	@Override
	public float z(int index) {
		return MemoryUtil.memGetFloat(ptr + (long) index * STRIDE + 8);
	}

	@Override
	public float r(int index) {
		return 1;
	}

	@Override
	public float g(int index) {
		return 1;
	}

	@Override
	public float b(int index) {
		return 1;
	}

	@Override
	public float a(int index) {
		return 1;
	}

	@Override
	public float u(int index) {
		return 0;
	}

	@Override
	public float v(int index) {
		return 0;
	}

	@Override
	public int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	public int light(int index) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	public float normalX(int index) {
		return 0;
	}

	@Override
	public float normalY(int index) {
		return 1;
	}

	@Override
	public float normalZ(int index) {
		return 0;
	}

	@Override
	public void x(int index, float x) {
		MemoryUtil.memPutFloat(ptr + (long) index * STRIDE, x);
	}

	@Override
	public void y(int index, float y) {
		MemoryUtil.memPutFloat(ptr + (long) index * STRIDE + 4, y);
	}

	@Override
	public void z(int index, float z) {
		MemoryUtil.memPutFloat(ptr + (long) index * STRIDE + 8, z);
	}

	@Override
	public void r(int index, float r) {
	}

	@Override
	public void g(int index, float g) {
	}

	@Override
	public void b(int index, float b) {
	}

	@Override
	public void a(int index, float a) {
	}

	@Override
	public void u(int index, float u) {
	}

	@Override
	public void v(int index, float v) {
	}

	@Override
	public void overlay(int index, int overlay) {
	}

	@Override
	public void light(int index, int light) {
	}

	@Override
	public void normalX(int index, float normalX) {
	}

	@Override
	public void normalY(int index, float normalY) {
	}

	@Override
	public void normalZ(int index, float normalZ) {
	}

	@Override
	public void write(MutableVertexList dst, int srcIndex, int dstIndex) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PositionOnlyVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr + srcIndex * STRIDE, dstPtr + dstIndex * STRIDE, STRIDE);
		} else {
			super.write(dst, srcIndex, dstIndex);
		}
	}

	@Override
	public void write(MutableVertexList dst, int srcStartIndex, int dstStartIndex, int vertexCount) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PositionOnlyVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr + srcStartIndex * STRIDE, dstPtr + dstStartIndex * STRIDE, vertexCount * STRIDE);
		} else {
			super.write(dst, srcStartIndex, dstStartIndex, vertexCount);
		}
	}

	@Override
	public void writeAll(MutableVertexList dst) {
		if (getClass() == dst.getClass()) {
			long dstPtr = ((PositionOnlyVertexList) dst).ptr;
			MemoryUtil.memCopy(ptr, dstPtr, vertexCount * STRIDE);
		} else {
			super.writeAll(dst);
		}
	}
}
