package com.jozufozu.flywheel.core.hardcoded;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.core.vertex.PosTexNormalVertex;
import com.jozufozu.flywheel.core.vertex.PosTexNormalWriterUnsafe;
import com.jozufozu.flywheel.util.joml.Vector4f;
import com.jozufozu.flywheel.util.joml.Vector4fc;

public class ModelPart implements Mesh {

	private final int vertices;
	private final String name;
	private final VertexList reader;
	private final @NotNull Vector4f boundingSphere;

	public ModelPart(List<PartBuilder.CuboidBuilder> cuboids, String name) {
		this.name = name;

		{
			int vertices = 0;
			for (PartBuilder.CuboidBuilder cuboid : cuboids) {
				vertices += cuboid.vertices();
			}
			this.vertices = vertices;
		}

		try (var stack = MemoryStack.stackPush()) {
			PosTexNormalWriterUnsafe writer = getVertexType().createWriter(stack.malloc(size()));
			for (PartBuilder.CuboidBuilder cuboid : cuboids) {
				cuboid.buffer(writer);
			}

			reader = writer.intoReader(this.vertices);
		}

		boundingSphere = ModelUtil.computeBoundingSphere(reader);
	}

	public static PartBuilder builder(String name, int sizeU, int sizeV) {
		return new PartBuilder(name, sizeU, sizeV);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int getVertexCount() {
		return vertices;
	}

	@Override
	public VertexList getReader() {
		return reader;
	}

	@Override
	public PosTexNormalVertex getVertexType() {
		return Formats.POS_TEX_NORMAL;
	}

	@Override
	public Vector4fc getBoundingSphere() {
		return boundingSphere;
	}
}
