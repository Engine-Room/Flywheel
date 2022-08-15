package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.instancing.ElementBuffer;
import com.jozufozu.flywheel.core.QuadConverter;

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
		return getVertexType().byteOffset(getVertexCount());
	}

	/**
	 * Write this mesh into memory. The written data will use the format defined by {@link #getVertexType()} and the amount of
	 * bytes written will be the same as the return value of {@link #size()}.
	 * @param ptr The address to which data is written to.
	 */
	void write(long ptr);

	/**
	 * Write this mesh into a vertex list. Vertices with index {@literal <}0 or {@literal >=}{@link #getVertexCount()} will not be
	 * modified.
	 * @param vertexList The vertex list to which data is written to.
	 */
	void write(MutableVertexList vertexList);

	/**
	 * Create an element buffer object that indexes the vertices of this mesh.
	 *
	 * <p>
	 *     Very often models in minecraft are made up of sequential quads, which is a very predictable pattern.
	 *     The default implementation accommodates this, however this can be overridden to change the behavior and
	 *     support more complex models.
	 * </p>
	 * @return an element buffer object indexing this model's vertices.
	 */
	default ElementBuffer createEBO() {
		return QuadConverter.getInstance()
				.quads2Tris(getVertexCount() / 4);
	}

	void close();

	/**
	 * A name uniquely identifying this mesh.
	 */
	String name();
}
