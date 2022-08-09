package com.jozufozu.flywheel.core.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.util.RenderMath;

import net.minecraft.client.renderer.texture.OverlayTexture;

public final class InferredVertexListImpl extends InferredVertexFormatInfo implements ReusableVertexList {
	private long ptr;
	private int vertexCount;

	public InferredVertexListImpl(InferredVertexFormatInfo formatInfo) {
		super(formatInfo);
	}

	private long idxPtr(int index) {
		return ptr + index * stride;
	}

	@Override
	public float x(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(idxPtr(index) + positionOffset);
	}

	@Override
	public float y(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(idxPtr(index) + positionOffset + 4);
	}

	@Override
	public float z(int index) {
		if (positionOffset < 0) return 0;
		return MemoryUtil.memGetFloat(idxPtr(index) + positionOffset + 8);
	}

	@Override
	public byte r(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(idxPtr(index) + colorOffset);
	}

	@Override
	public byte g(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(idxPtr(index) + colorOffset + 1);
	}

	@Override
	public byte b(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(idxPtr(index) + colorOffset + 2);
	}

	@Override
	public byte a(int index) {
		if (colorOffset < 0) return 0;
		return MemoryUtil.memGetByte(idxPtr(index) + colorOffset + 3);
	}

	@Override
	public float u(int index) {
		if (textureOffset < 0) return 0;
		return MemoryUtil.memGetFloat(idxPtr(index) + textureOffset);
	}

	@Override
	public float v(int index) {
		if (textureOffset < 0) return 0;
		return MemoryUtil.memGetFloat(idxPtr(index) + textureOffset + 4);
	}

	@Override
	public int overlay(int index) {
		if (overlayOffset < 0) return OverlayTexture.NO_OVERLAY;
		return MemoryUtil.memGetInt(idxPtr(index) + overlayOffset);
	}

	@Override
	public int light(int index) {
		if (lightOffset < 0) return 0;
		return MemoryUtil.memGetInt(idxPtr(index) + lightOffset);
	}

	@Override
	public float normalX(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + normalOffset));
	}

	@Override
	public float normalY(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + normalOffset + 1));
	}

	@Override
	public float normalZ(int index) {
		if (normalOffset < 0) return 0;
		return RenderMath.f(MemoryUtil.memGetByte(idxPtr(index) + normalOffset + 2));
	}

	@Override
	public void x(int index, float x) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(idxPtr(index) + positionOffset, x);
	}

	@Override
	public void y(int index, float y) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(idxPtr(index) + positionOffset + 4, y);
	}

	@Override
	public void z(int index, float z) {
		if (positionOffset < 0) return;
		MemoryUtil.memPutFloat(idxPtr(index) + positionOffset + 8, z);
	}

	@Override
	public void r(int index, byte r) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + colorOffset, r);
	}

	@Override
	public void g(int index, byte g) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + colorOffset + 1, g);
	}

	@Override
	public void b(int index, byte b) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + colorOffset + 2, b);
	}

	@Override
	public void a(int index, byte a) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + colorOffset + 3, a);
	}

	@Override
	public void u(int index, float u) {
		if (textureOffset < 0) return;
		MemoryUtil.memPutFloat(idxPtr(index) + textureOffset, u);
	}

	@Override
	public void v(int index, float v) {
		if (textureOffset < 0) return;
		MemoryUtil.memPutFloat(idxPtr(index) + textureOffset + 4, v);
	}

	@Override
	public void overlay(int index, int overlay) {
		if (overlayOffset < 0) return;
		MemoryUtil.memPutInt(idxPtr(index) + overlayOffset, overlay);
	}

	@Override
	public void light(int index, int light) {
		if (lightOffset < 0) return;
		MemoryUtil.memPutInt(idxPtr(index) + lightOffset, light);
	}

	@Override
	public void normalX(int index, float normalX) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + normalOffset, RenderMath.nb(normalX));
	}

	@Override
	public void normalY(int index, float normalY) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + normalOffset + 1, RenderMath.nb(normalY));
	}

	@Override
	public void normalZ(int index, float normalZ) {
		if (normalOffset < 0) return;
		MemoryUtil.memPutByte(idxPtr(index) + normalOffset + 2, RenderMath.nb(normalZ));
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public long ptr() {
		return ptr;
	}

	@Override
	public void ptr(long ptr) {
		this.ptr = ptr;
	}

	@Override
	public void shiftPtr(int vertices) {
		ptr += vertices * stride;
	}

	@Override
	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}
}
