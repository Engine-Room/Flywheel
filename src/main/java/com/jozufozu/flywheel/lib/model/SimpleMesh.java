package com.jozufozu.flywheel.lib.model;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class SimpleMesh implements QuadMesh {
	private final VertexType vertexType;
	private final int vertexCount;
	private final MemoryBlock data;
	private final ReusableVertexList vertexList;
	private final Vector4f boundingSphere;
	@Nullable
	private final String descriptor;

	public SimpleMesh(VertexType vertexType, MemoryBlock data, @Nullable String descriptor) {
		this.vertexType = vertexType;
		this.data = data;
		this.descriptor = descriptor;

		int bytes = (int) data.size();
		int stride = vertexType.getLayout().getStride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("MemoryBlock contains non-whole amount of vertices!");
		}
		vertexCount = bytes / stride;

		vertexList = vertexType().createVertexList();
		vertexList.ptr(data.ptr());
		vertexList.vertexCount(vertexCount);

		boundingSphere = ModelUtil.computeBoundingSphere(vertexList);
	}

	public SimpleMesh(VertexType vertexType, MemoryBlock data) {
		this(vertexType, data, null);
	}

	@Override
	public VertexType vertexType() {
		return vertexType;
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public void write(long ptr) {
		data.copyTo(ptr);
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
	public void delete() {
		data.free();
	}

	@Override
	public String toString() {
		return "SimpleMesh{" + "vertexType=" + vertexType + ",vertexCount=" + vertexCount + ",descriptor={" + descriptor + "}" + "}";
	}
}
