package com.jozufozu.flywheel.backend.instancing.batching;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.util.RenderMath;

public class MutableVertexListImpl extends VertexFormatInfo implements MutableVertexList {
	private final long anchorPtr;
	private final int totalVertexCount;

	private long ptr;
	private int vertexCount;

	public MutableVertexListImpl(long ptr, VertexFormatInfo formatInfo, int vertexCount) {
		super(formatInfo);

		anchorPtr = ptr;
		totalVertexCount = vertexCount;

		setFullRange();
	}

	public void setRange(int startVertex, int vertexCount) {
		ptr = anchorPtr + startVertex * stride;
		this.vertexCount = vertexCount;
	}

	public void setFullRange() {
		ptr = anchorPtr;
		vertexCount = totalVertexCount;
	}

	@Override
	public float x(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(ptr + index * stride + positionOffset);
	}

	@Override
	public float y(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(ptr + index * stride + positionOffset + 4);
	}

	@Override
	public float z(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(ptr + index * stride + positionOffset + 8);
	}

	@Override
	public byte r(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(ptr + index * stride + colorOffset);
	}

	@Override
	public byte g(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 1);
	}

	@Override
	public byte b(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 2);
	}

	@Override
	public byte a(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 3);
	}

	@Override
	public float u(int index) {
		if (textureOffset < 0) return 0;
		return MemoryUtil.memGetFloat(ptr + index * stride + textureOffset);
	}

	@Override
	public float v(int index) {
		if (textureOffset < 0) return 0;
		return MemoryUtil.memGetFloat(ptr + index * stride + textureOffset + 4);
	}

	@Override
	public int overlay(int index) {
		if (overlayOffset < 0) return 0;
		return MemoryUtil.memGetInt(ptr + index * stride + overlayOffset);
	}

	@Override
	public int light(int index) {
		if (lightOffset < 0) return 0;
		return MemoryUtil.memGetInt(ptr + index * stride + lightOffset);
	}

	@Override
	public float normalX(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * stride + normalOffset));
	}

	@Override
	public float normalY(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * stride + normalOffset + 1));
	}

	@Override
	public float normalZ(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(ptr + index * stride + normalOffset + 2));
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void x(int index, float x) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(ptr + index * stride + positionOffset, x);
	}

	@Override
	public void y(int index, float y) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(ptr + index * stride + positionOffset + 4, y);
	}

	@Override
	public void z(int index, float z) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(ptr + index * stride + positionOffset + 8, z);
	}

	@Override
	public void r(int index, byte r) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset, r);
	}

	@Override
	public void g(int index, byte g) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 1, g);
	}

	@Override
	public void b(int index, byte b) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 2, b);
	}

	@Override
	public void a(int index, byte a) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 3, a);
	}

	@Override
	public void u(int index, float u) {
		if (textureOffset < 0) return;
		MemoryUtil.memPutFloat(ptr + index * stride + textureOffset, u);
	}

	@Override
	public void v(int index, float v) {
		if (textureOffset < 0) return;
		MemoryUtil.memPutFloat(ptr + index * stride + textureOffset + 4, v);
	}

	@Override
	public void overlay(int index, int overlay) {
		if (overlayOffset < 0) return;
		MemoryUtil.memPutInt(ptr + index * stride + overlayOffset, overlay);
	}

	@Override
	public void light(int index, int light) {
		if (lightOffset < 0) return;
		MemoryUtil.memPutInt(ptr + index * stride + lightOffset, light);
	}

	@Override
	public void normalX(int index, float normalX) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + normalOffset, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + normalOffset + 1, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + normalOffset + 2, RenderMath.nb(normalZ));
	}
}
