package com.jozufozu.flywheel.api.vertex;

public interface VertexView extends MutableVertexList {
	long ptr();

	void ptr(long ptr);

	void vertexCount(int vertexCount);

	long stride();
}
