package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.api.vertex.VertexType;

public interface BufferedModel {

	VertexType getType();

	int getVertexCount();

	/**
	 * The VBO/VAO should be bound externally.
	 */
	void setupState();

	void clearState();

	void drawCall();

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	void drawInstances(int instanceCount);

	boolean isDeleted();

	void delete();

	default BufferLayout getFormat() {
		return getType().getLayout();
	}

	default boolean valid() {
		return getVertexCount() > 0 && !isDeleted();
	}

	default int getAttributeCount() {
		return getFormat().getAttributeCount();
	}
}
