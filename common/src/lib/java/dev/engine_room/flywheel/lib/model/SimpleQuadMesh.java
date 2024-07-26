package dev.engine_room.flywheel.lib.model;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.vertex.VertexList;

public final class SimpleQuadMesh implements QuadMesh {
	private final VertexList vertexList;
	private final Vector4f boundingSphere;
	@Nullable
	private final String descriptor;

	public SimpleQuadMesh(VertexList vertexList, @Nullable String descriptor) {
		this.vertexList = vertexList;
		boundingSphere = ModelUtil.computeBoundingSphere(vertexList);
		this.descriptor = descriptor;
	}

	public SimpleQuadMesh(VertexList vertexList) {
		this(vertexList, null);
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
