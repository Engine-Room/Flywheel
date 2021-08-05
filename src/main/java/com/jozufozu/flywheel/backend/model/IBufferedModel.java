package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public interface IBufferedModel {

	VertexFormat getFormat();

	int getVertexCount();

	boolean valid();

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

	void delete();

	default int getAttributeCount() {
		return getFormat().getAttributeCount();
	}
}
