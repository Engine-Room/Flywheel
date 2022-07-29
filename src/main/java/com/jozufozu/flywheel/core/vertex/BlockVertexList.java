package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.texture.OverlayTexture;

public class BlockVertexList extends AbstractVertexList {

	private final int stride;

	public BlockVertexList(BufferBuilder builder) {
		super(builder);
		this.stride = builder.getVertexFormat()
				.getVertexSize();
	}

	@Override
	public boolean isEmpty() {
		return vertexCount == 0;
	}

	private int vertIdx(int vertexIndex) {
		return vertexIndex * stride;
	}

	@Override
	public float x(int index) {
		return contents.getFloat(vertIdx(index));
	}

	@Override
	public float y(int index) {
		return contents.getFloat(vertIdx(index) + 4);
	}

	@Override
	public float z(int index) {
		return contents.getFloat(vertIdx(index) + 8);
	}

	@Override
	public byte r(int index) {
		return contents.get(vertIdx(index) + 12);
	}

	@Override
	public byte g(int index) {
		return contents.get(vertIdx(index) + 13);
	}

	@Override
	public byte b(int index) {
		return contents.get(vertIdx(index) + 14);
	}

	@Override
	public byte a(int index) {
		return contents.get(vertIdx(index) + 15);
	}

	@Override
	public float u(int index) {
		return contents.getFloat(vertIdx(index) + 16);
	}

	@Override
	public float v(int index) {
		return contents.getFloat(vertIdx(index) + 20);
	}

	@Override
	public int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	public int light(int index) {
		return contents.getInt(vertIdx(index) + 24);
	}

	@Override
	public float normalX(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 28));
	}

	@Override
	public float normalY(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 29));
	}

	@Override
	public float normalZ(int index) {
		return RenderMath.f(contents.get(vertIdx(index) + 30));
	}

}
