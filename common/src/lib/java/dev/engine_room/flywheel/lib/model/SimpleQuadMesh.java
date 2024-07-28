package dev.engine_room.flywheel.lib.model;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public final class SimpleQuadMesh implements QuadMesh {
	private final VertexList vertexList;
	// Unused but we need to hold on to a reference so the cleaner doesn't nuke us.
	private final MemoryBlock data;
	private final Vector4f boundingSphere;
	@Nullable
	private final String descriptor;

	public SimpleQuadMesh(VertexList vertexList, MemoryBlock data, @Nullable String descriptor) {
		this.vertexList = vertexList;
		this.data = data;
		boundingSphere = ModelUtil.computeBoundingSphere(vertexList);
		this.descriptor = descriptor;
	}

	public SimpleQuadMesh(VertexList vertexList, MemoryBlock data) {
		this(vertexList, data, null);
	}

	@Override
	public int vertexCount() {
		return vertexList.vertexCount();
	}

	@Override
	public void write(MutableVertexList dst) {
		vertexList.writeAll(dst);
	}

	@Override
	public Vector4fc boundingSphere() {
		return boundingSphere;
	}

	@Override
	public String toString() {
		return "SimpleQuadMesh{" + "vertexCount=" + vertexCount() + ",descriptor={" + descriptor + "}" + "}";
	}
}
