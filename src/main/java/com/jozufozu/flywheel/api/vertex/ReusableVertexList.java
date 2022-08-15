package com.jozufozu.flywheel.api.vertex;

public interface ReusableVertexList extends MutableVertexList {
	long ptr();

	void ptr(long ptr);

	void shiftPtr(int vertices);

	void setVertexCount(int vertexCount);
}
