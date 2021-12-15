package com.jozufozu.flywheel.core.model;

import static com.jozufozu.flywheel.util.RenderMath.nb;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class VecBufferWriter implements VertexConsumer {

	private final VecBuffer buffer;

	public VecBufferWriter(ByteBuffer buffer) {
		this.buffer = new VecBuffer(buffer);
	}

	public VecBufferWriter(VecBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public VertexConsumer vertex(double v, double v1, double v2) {
		buffer.putVec3((float) v, (float) v1, (float) v2);
		return this;
	}

	@Override
	public VertexConsumer color(int i, int i1, int i2, int i3) {
		buffer.putColor(i, i1, i2, i3);
		return this;
	}

	@Override
	public VertexConsumer uv(float v, float v1) {
		buffer.putVec2(v, v1);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int i, int i1) {
		return this;
	}

	@Override
	public VertexConsumer uv2(int i, int i1) {
		buffer.putVec2((byte) i, (byte) i1);
		return this;
	}

	@Override
	public VertexConsumer normal(float v, float v1, float v2) {
		buffer.putVec3(nb(v), nb(v1), nb(v2));
		return this;
	}

	@Override
	public void endVertex() {

	}

	@Override
	public void defaultColor(int i, int i1, int i2, int i3) {

	}

	@Override
	public void unsetDefaultColor() {

	}
}
