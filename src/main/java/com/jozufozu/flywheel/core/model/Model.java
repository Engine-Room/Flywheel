package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.model.ElementBuffer;
import com.jozufozu.flywheel.core.QuadConverter;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A model that can be rendered by flywheel.
 *
 * <p>
 *     It is expected that the following assertion will not fail:
 * </p>
 *
 * <pre>{@code
 * IModel model = ...;
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

	/**
	 * Copy this model into the given buffer.
	 */
	void buffer(VertexConsumer buffer);

	/**
	 * @return The number of vertices the model has.
	 */
	int vertexCount();

	/**
	 * @return The format of this model's vertices
	 */
	VertexFormat format();

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

	/**
	 * The size in bytes that this model's data takes up.
	 */
	default int size() {
		return vertexCount() * format().getStride();
	}

	/**
	 * Is there nothing to render?
	 * @return true if there are no vertices.
	 */
	default boolean empty() {
		return vertexCount() == 0;
	}
}
