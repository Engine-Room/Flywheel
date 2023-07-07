package com.jozufozu.flywheel.core.hardcoded;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;

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

		ByteBuffer buffer = MemoryUtil.memAlloc(size());
		PosTexNormalWriterUnsafe writer = Formats.POS_TEX_NORMAL.createWriter(buffer);
		for (PartBuilder.CuboidBuilder cuboid : cuboids) {
			cuboid.buffer(writer);
		}

		reader = writer.intoReader();
		MemoryUtil.memFree(buffer);
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
	public VertexType getType() {
		return Formats.POS_TEX_NORMAL;
	}

	@Override
	public void delete() {
		reader.delete();
	}
}
