package com.jozufozu.flywheel.api.model;

import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;

/**
 * A holder for arbitrary vertex data that can be written to memory or a vertex list.
 */
public interface Mesh {
	VertexType vertexType();

	/**
	 * @return The number of vertices this mesh has.
	 */
	int vertexCount();

	/**
	 * Is there nothing to render?
	 * @return true if there are no vertices.
	 */
	default boolean isEmpty() {
		return vertexCount() == 0;
	}

	/**
	 * The size in bytes that this mesh's data takes up.
	 */
	default int size() {
		return vertexType().getStride() * vertexCount();
	}

	/**
	 * Write this mesh into memory. The written data will use the format defined by {@link #vertexType()} and the amount of
	 * bytes written will be the same as the return value of {@link #size()}.
	 *
	 * @param ptr The address to which data is written to.
	 */
	void write(long ptr);

	/**
	 * Write this mesh into a vertex list. Vertices with index {@literal <}0 or {@literal >=}{@link #vertexCount()} will not be
	 * read or modified.
	 *
	 * @param vertexList The vertex list to which data is written to.
	 */
	void write(MutableVertexList vertexList);

	IndexSequence indexSequence();

	int indexCount();

	/**
	 * Get a vec4 representing this mesh's bounding sphere in the format (x, y, z, radius).
	 *
	 * @return A vec4 view.
	 */
	Vector4fc boundingSphere();

	/**
	 * Free this mesh's resources, memory, etc.
	 */
	void delete();
}
