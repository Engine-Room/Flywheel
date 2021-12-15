package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;
import java.util.List;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.util.ModelReader;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

public class ModelPart implements Model {

	private final List<PartBuilder.CuboidBuilder> cuboids;
	private int vertices;
	private final String name;

	public ModelPart(List<PartBuilder.CuboidBuilder> cuboids, String name) {
		this.cuboids = cuboids;
		this.name = name;

		vertices = 0;

		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			vertices += cuboid.vertices();
		}
	}

	public static PartBuilder builder(String name, int sizeU, int sizeV) {
		return new PartBuilder(name, sizeU, sizeV);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void buffer(VertexConsumer buffer) {
		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			cuboid.buffer(buffer);
		}
	}

	@Override
	public int vertexCount() {
		return vertices;
	}

	@Override
	public VertexFormat format() {
		return Formats.UNLIT_MODEL;
	}

	@Override
	public ModelReader getReader() {
		return new PartReader(this);
	}

	private class PartReader implements ModelReader {

		private final ByteBuffer buffer;

		private PartReader(ModelPart part) {
			this.buffer = ByteBuffer.allocate(part.size());
			VecBufferWriter writer = new VecBufferWriter(this.buffer);

			buffer(writer);
		}

		private int vertIdx(int vertexIndex) {
			return vertexIndex * format().getStride();
		}

		@Override
		public float getX(int index) {
			return buffer.getFloat(vertIdx(index));
		}

		@Override
		public float getY(int index) {
			return buffer.getFloat(vertIdx(index) + 4);
		}

		@Override
		public float getZ(int index) {
			return buffer.getFloat(vertIdx(index) + 8);
		}

		@Override
		public byte getR(int index) {
			return (byte) 0xFF;
		}

		@Override
		public byte getG(int index) {
			return (byte) 0xFF;
		}

		@Override
		public byte getB(int index) {
			return (byte) 0xFF;
		}

		@Override
		public byte getA(int index) {
			return (byte) 0xFF;
		}

		@Override
		public float getU(int index) {
			return buffer.getFloat(vertIdx(index) + 15);
		}

		@Override
		public float getV(int index) {
			return buffer.getFloat(vertIdx(index) + 19);
		}

		@Override
		public int getLight(int index) {
			return 0;
		}

		@Override
		public float getNX(int index) {
			return RenderMath.f(buffer.get(vertIdx(index) + 12));
		}

		@Override
		public float getNY(int index) {
			return RenderMath.f(buffer.get(vertIdx(index) + 13));
		}

		@Override
		public float getNZ(int index) {
			return RenderMath.f(buffer.get(vertIdx(index) + 14));
		}

		@Override
		public int getVertexCount() {
			return vertices;
		}
	}
}
