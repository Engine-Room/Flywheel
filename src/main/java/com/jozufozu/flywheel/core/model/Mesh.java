package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.api.vertex.VertexWriter;
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

	/**
	 * A name uniquely identifying this model.
	 */
	String name();

	VertexType getVertexType();

	VertexList getReader();

	/**
	 * @return The number of vertices the model has.
	 */
	default int getVertexCount() {
		return getReader().getVertexCount();
	}

	/**
	 * Is there nothing to render?
	 * @return true if there are no vertices.
	 */
	default boolean isEmpty() {
		return getReader().isEmpty();
	}

	/**
	 * The size in bytes that this model's data takes up.
	 */
	default int size() {
		return getVertexType().byteOffset(getVertexCount());
	}

	/**
	 * Create an element buffer object that indexes the vertices of this model.
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

	default void writeInto(ByteBuffer buffer, long byteIndex) {
		VertexWriter writer = getVertexType().createWriter(buffer);
		writer.seek(byteIndex);
		writer.writeVertexList(getReader());
	}
}
