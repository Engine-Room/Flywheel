package com.jozufozu.flywheel.core.model;

import java.util.List;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.core.Formats;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
}
