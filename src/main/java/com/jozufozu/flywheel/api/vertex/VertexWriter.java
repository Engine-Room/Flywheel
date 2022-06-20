package com.jozufozu.flywheel.api.vertex;

public interface VertexWriter {
	void writeVertex(VertexList list, int index);

	void seek(long offset);

	VertexList intoReader(int vertices);

	default void writeVertexList(VertexList list) {
		for (int i = 0; i < list.getVertexCount(); i++) {
			this.writeVertex(list, i);
		}
	}
}
