package com.jozufozu.flywheel.api.vertex;

public interface VertexWriter {
	void writeVertex(VertexList list, int index);

	void seekToVertex(int vertex);

	VertexList intoReader();

	default void writeVertexList(VertexList list) {
		for (int i = 0; i < list.getVertexCount(); i++) {
			this.writeVertex(list, i);
		}
	}
}
