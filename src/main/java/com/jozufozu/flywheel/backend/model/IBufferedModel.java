package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public interface IBufferedModel {

	VertexFormat getFormat();

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

	default boolean valid() {
		return getVertexCount() > 0 && !isDeleted();
	}

	default int getAttributeCount() {
		return getFormat().getAttributeCount();
	}
}
