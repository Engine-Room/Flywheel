package com.jozufozu.flywheel.core.hardcoded;

import java.util.List;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertex;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import com.mojang.blaze3d.platform.MemoryTracker;

public class ModelPart implements Mesh {

	private final int vertices;
	private final String name;
	private final VertexList reader;

	public ModelPart(List<PartBuilder.CuboidBuilder> cuboids, String name) {
		this.name = name;

		{
			int vertices = 0;
			for (PartBuilder.CuboidBuilder cuboid : cuboids) {
				vertices += cuboid.vertices();
			}
			this.vertices = vertices;
		}

		PosTexNormalWriterUnsafe writer = getType().createWriter(MemoryTracker.create(size()));
		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			cuboid.buffer(writer);
		}

		reader = writer.intoReader();
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
	public VertexList getReader() {
		return reader;
	}

	@Override
	public PosTexNormalVertex getType() {
		return Formats.POS_TEX_NORMAL;
	}
}
