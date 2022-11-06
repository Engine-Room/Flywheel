package com.jozufozu.flywheel.core.hardcoded;

import java.util.List;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import com.mojang.blaze3d.platform.MemoryTracker;

public class ModelPart implements Model {

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

		PosTexNormalWriterUnsafe writer = Formats.POS_TEX_NORMAL.createWriter(MemoryTracker.create(size()));
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
	public ElementBuffer createEBO() {
		return QuadConverter.getInstance()
				.quads2Tris(vertices / 4);
	}

	@Override
	public void delete() {
		if (reader instanceof AutoCloseable closeable) {
			try {
				closeable.close();
			} catch (Exception e) {
				//
			}
		}
	}
}
