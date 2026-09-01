package dev.engine_room.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class EmptyVertexConsumer implements VertexConsumer {
	public static final EmptyVertexConsumer INSTANCE = new EmptyVertexConsumer();

	private EmptyVertexConsumer() {}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		return this;
	}
}
