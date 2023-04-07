package com.jozufozu.flywheel.lib.model;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

public class SimpleMesh implements QuadMesh {
	private final VertexType vertexType;
	private final int vertexCount;
	private final MemoryBlock contents;
	private final ReusableVertexList vertexList;
	private final Vector4f boundingSphere;
	private final String name;

	public SimpleMesh(VertexType vertexType, MemoryBlock contents, String name) {
		this.vertexType = vertexType;
		this.contents = contents;
		this.name = name;

		int bytes = (int) contents.size();
		int stride = vertexType.getLayout().getStride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("MemoryBlock contains non-whole amount of vertices!");
		}
		vertexCount = bytes / stride;

		vertexList = getVertexType().createVertexList();
		vertexList.ptr(contents.ptr());
		vertexList.vertexCount(vertexCount);

		boundingSphere = ModelUtil.computeBoundingSphere(vertexList);
	}

	@Override
	public VertexType getVertexType() {
		return vertexType;
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void write(long ptr) {
		contents.copyTo(ptr);
	}

	@Override
	public void write(MutableVertexList dst) {
		vertexList.writeAll(dst);
	}

	@Override
	public Vector4fc getBoundingSphere() {
		return boundingSphere;
	}

	@Override
	public void delete() {
		contents.free();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "SimpleMesh{" + "name='" + name + "',vertexType='" + vertexType + "}";
	}
}
