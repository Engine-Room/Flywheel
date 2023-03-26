package com.jozufozu.flywheel.api.vertex;

public interface ReusableVertexList extends MutableVertexList {
	long ptr();

	void ptr(long ptr);

	void vertexCount(int vertexCount);
}
