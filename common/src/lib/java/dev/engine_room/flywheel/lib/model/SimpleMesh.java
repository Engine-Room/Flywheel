package dev.engine_room.flywheel.lib.model;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.vertex.VertexView;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class SimpleMesh implements QuadMesh {
	private final int vertexCount;
	private final VertexView vertexView;
	private final Vector4f boundingSphere;
	private final MemoryBlock data;
	@Nullable
	private final String descriptor;

	public SimpleMesh(VertexView vertexView, MemoryBlock data, @Nullable String descriptor) {
		this.vertexView = vertexView;
		this.data = data;
		this.descriptor = descriptor;

		int bytes = (int) data.size();
		int stride = (int) this.vertexView.stride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("MemoryBlock contains non-whole amount of vertices!");
		}
		vertexCount = bytes / stride;

		this.vertexView.ptr(data.ptr());
		this.vertexView.vertexCount(vertexCount);

		boundingSphere = ModelUtil.computeBoundingSphere(vertexView);
	}

	public SimpleMesh(VertexView vertexView, MemoryBlock data) {
		this(vertexView, data, null);
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public void write(MutableVertexList dst) {
		vertexView.writeAll(dst);
	}

	@Override
	public Vector4fc boundingSphere() {
		return boundingSphere;
	}

	@Override
	public void delete() {
		data.free();
	}

	@Override
	public String toString() {
		return "SimpleMesh{" + "vertexCount=" + vertexCount + ",descriptor={" + descriptor + "}" + "}";
	}
}
