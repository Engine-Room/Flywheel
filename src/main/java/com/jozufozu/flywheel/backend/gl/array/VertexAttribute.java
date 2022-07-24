package com.jozufozu.flywheel.backend.gl.array;

public interface VertexAttribute {
	int getByteWidth();

	void pointer(long offset, int i, int stride);
}
