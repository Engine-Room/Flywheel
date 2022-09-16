package com.jozufozu.flywheel.api.vertex;

public interface ReusableVertexList extends MutableVertexList {
	long ptr();

	void ptr(long ptr);

	int vertexStride();

	void vertexCount(int vertexCount);
}
