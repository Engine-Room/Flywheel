package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.instancing.ElementBuffer;
import com.jozufozu.flywheel.core.QuadConverter;

/**
 * A mesh that can be rendered by flywheel.
 *
 * <p>
 *     It is expected that the following assertion will not fail:
 * </p>
 *
 * <pre>{@code
 * Mesh mesh = ...;
 * VecBuffer into = ...;
 *
 * int initial = VecBuffer.unwrap().position();
 *
 * mesh.buffer(into);
 *
 * int final = VecBuffer.unwrap().position();
 *
 * assert mesh.size() == final - initial;
 * }</pre>
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

	void write(long ptr);

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
