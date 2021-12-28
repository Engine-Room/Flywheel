package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.layout.BufferLayout;

public interface BufferedModel {

	VertexType getType();

	int getVertexCount();

	/**
	 * The VAO must be bound externally.
	 */
	void setupState(GlVertexArray vao);

	void drawCall();

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	void drawInstances(int instanceCount);

	boolean isDeleted();

	void delete();

	default BufferLayout getLayout() {
		return getType().getLayout();
	}

	default boolean valid() {
		return getVertexCount() > 0 && !isDeleted();
	}

	default int getAttributeCount() {
		return getType().getLayout().getAttributeCount();
	}
}
