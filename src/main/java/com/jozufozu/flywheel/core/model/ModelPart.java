package com.jozufozu.flywheel.core.model;

import java.util.List;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.QuadConverter;

public class ModelPart implements IModel {

	private final List<PartBuilder.CuboidBuilder> cuboids;
	private int vertices;

	public ModelPart(List<PartBuilder.CuboidBuilder> cuboids) {
		this.cuboids = cuboids;

		vertices = 0;

		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			vertices += cuboid.vertices();
		}
	}

	public static PartBuilder builder(int sizeU, int sizeV) {
		return new PartBuilder(sizeU, sizeV);
	}

	@Override
	public void buffer(VecBuffer buffer) {
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
	public ElementBuffer createEBO() {
		return QuadConverter.getInstance()
				.quads2Tris(vertices / 4);
	}
}
