package com.jozufozu.flywheel.api.vertex;

/**
 * A vertex type containing metadata about a specific vertex layout.
 */
// TODO: query a bitset of vertex attributes that are used?
public interface VertexType extends VertexListProvider {
	/**
	 * The byte size of a single vertex.
	 */
	int getStride();

	default int byteOffset(int vertexIndex) {
		return getStride() * vertexIndex;
	}
}
