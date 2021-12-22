package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;
import java.util.List;

import com.jozufozu.flywheel.core.vertex.PosNormalTexReader;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriter;
import com.jozufozu.flywheel.util.ModelReader;
import com.mojang.blaze3d.platform.MemoryTracker;

public class ModelPart implements Model {

	private final int vertices;
	private final String name;
	private final PosNormalTexReader reader;

	public ModelPart(List<PartBuilder.CuboidBuilder> cuboids, String name) {
		this.name = name;

		{
			int vertices = 0;
			for (PartBuilder.CuboidBuilder cuboid : cuboids) {
				vertices += cuboid.vertices();
			}
			this.vertices = vertices;
		}

		ByteBuffer buffer = MemoryTracker.create(size());
		PosTexNormalWriter writer = new PosTexNormalWriter(buffer);
		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			cuboid.buffer(writer);
		}

		reader = new PosNormalTexReader(buffer, vertices);
	}

	public static PartBuilder builder(String name, int sizeU, int sizeV) {
		return new PartBuilder(name, sizeU, sizeV);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int vertexCount() {
		return vertices;
	}

	@Override
	public ModelReader getReader() {
		return reader;
	}
}
