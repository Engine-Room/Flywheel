package com.jozufozu.flywheel.impl.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.lib.math.RenderMath;
import com.jozufozu.flywheel.lib.vertex.AbstractVertexList;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class InferredVertexListImpl extends AbstractVertexList implements ReusableVertexList {
	protected final VertexFormat format;
	protected final int stride;

	protected final int positionOffset;
	protected final int colorOffset;
	protected final int textureOffset;
	protected final int overlayOffset;
	protected final int lightOffset;
	protected final int normalOffset;

	public InferredVertexListImpl(InferredVertexFormatInfo formatInfo) {
		format = formatInfo.format;
		stride = format.getVertexSize();
		positionOffset = formatInfo.positionOffset;
		colorOffset = formatInfo.colorOffset;
		textureOffset = formatInfo.textureOffset;
		overlayOffset = formatInfo.overlayOffset;
		lightOffset = formatInfo.lightOffset;
		normalOffset = formatInfo.normalOffset;
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
	public float r(int index) {
		if (colorOffset < 0) return 1;
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * stride + colorOffset));
	}

	@Override
	public float g(int index) {
		if (colorOffset < 0) return 1;
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 1));
	}

	@Override
	public float b(int index) {
		if (colorOffset < 0) return 1;
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 2));
	}

	@Override
	public float a(int index) {
		if (colorOffset < 0) return 1;
		return RenderMath.uf(MemoryUtil.memGetByte(ptr + index * stride + colorOffset + 3));
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
		if (overlayOffset < 0) return OverlayTexture.NO_OVERLAY;
		return MemoryUtil.memGetInt(ptr + index * stride + overlayOffset);
	}

	@Override
	public int light(int index) {
		if (lightOffset < 0) return LightTexture.FULL_BRIGHT;
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
	public void r(int index, float r) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset, RenderMath.unb(r));
	}

	@Override
	public void g(int index, float g) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 1, RenderMath.unb(g));
	}

	@Override
	public void b(int index, float b) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 2, RenderMath.unb(b));
	}

	@Override
	public void a(int index, float a) {
		if (colorOffset < 0) return;
		MemoryUtil.memPutByte(ptr + index * stride + colorOffset + 3, RenderMath.unb(a));
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
