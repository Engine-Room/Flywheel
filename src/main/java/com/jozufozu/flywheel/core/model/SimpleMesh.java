package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.memory.MemoryBlock;

public class SimpleMesh implements Mesh {
	private final VertexType vertexType;
	private final int vertexCount;
	private final MemoryBlock contents;
	private final ReusableVertexList vertexList;
	private final String name;

	public SimpleMesh(VertexType vertexType, MemoryBlock contents, String name) {
		this.vertexType = vertexType;
		this.contents = contents;
		this.name = name;

		int bytes = (int) contents.size();
		int stride = vertexType.getStride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("MemoryBlock contains non-whole amount of vertices!");
		}
		vertexCount = bytes / stride;

		vertexList = getVertexType().createVertexList();
		vertexList.ptr(contents.ptr());
		vertexList.setVertexCount(vertexCount);
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
	public void close() {
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
