package com.jozufozu.flywheel.api.model;

import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.gl.buffer.ElementBuffer;

/**
 * A holder for arbitrary vertex data that can be written to memory or a vertex list.
 */
public interface Mesh {
	VertexType getVertexType();

	/**
	 * @return The number of vertices this mesh has.
	 */
	int getVertexCount();

	/**
	 * Is there nothing to render?
	 * @return true if there are no vertices.
	 */
	default boolean isEmpty() {
		return getVertexCount() == 0;
	}

	/**
	 * The size in bytes that this mesh's data takes up.
	 */
	default int size() {
		return getVertexType().getLayout().getStride() * getVertexCount();
	}

	/**
	 * Write this mesh into memory. The written data will use the format defined by {@link #getVertexType()} and the amount of
	 * bytes written will be the same as the return value of {@link #size()}.
	 * @param ptr The address to which data is written to.
	 */
	void write(long ptr);

	/**
	 * Write this mesh into a vertex list. Vertices with index {@literal <}0 or {@literal >=}{@link #getVertexCount()} will not be
	 * read or modified.
	 * @param vertexList The vertex list to which data is written to.
	 */
	void write(MutableVertexList vertexList);

	/**
	 * Create an element buffer object that indexes the vertices of this mesh.
	 * @return an element buffer object indexing this model's vertices.
	 */
	ElementBuffer createEBO();

	Vector4fc getBoundingSphere();

	void delete();

	/**
	 * A name uniquely identifying this mesh.
	 */
	String name();
}
