package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.QuadConverter;

/**
 * A model that can be rendered by flywheel.
 *
 * <p>
 *     It is expected that the following assertion will not fail:
 * </p>
 *
 * <pre>{@code
 * Model model = ...;
 * VecBuffer into = ...;
 *
 * int initial = VecBuffer.unwrap().position();
 *
 * model.buffer(into);
 *
 * int final = VecBuffer.unwrap().position();
 *
 * assert model.size() == final - initial;
 * }</pre>
 */
public interface Model {

	/**
	 * A name uniquely identifying this model.
	 */
	String name();

	VertexList getReader();

	/**
	 * @return The number of vertices the model has.
	 */
	int vertexCount();

	VertexType getType();

	// XXX Since this is public API (technically) we cannot make assumptions about what GL state this method can use or modify unless a contract is established.
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
				.quads2Tris(vertexCount() / 4);
	}

	void delete();

	/**
	 * The size in bytes that this model's data takes up.
	 */
	default int size() {
		return getType().byteOffset(vertexCount());
	}

	/**
	 * Is there nothing to render?
	 * @return true if there are no vertices.
	 */
	default boolean empty() {
		return vertexCount() == 0;
	}

	default void writeInto(ByteBuffer buffer) {
		getType().createWriter(buffer).writeVertexList(getReader());
	}
}
