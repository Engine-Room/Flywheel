package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.model.ElementBuffer;

/**
 * A model that can be rendered by flywheel.
 */
public interface IModel {

	/**
	 * Copy this model into the given buffer.
	 */
	void buffer(VecBuffer buffer);

	int vertexCount();

	VertexFormat format();

	ElementBuffer createEBO();

	default int size() {
		return vertexCount() * format().getStride();
	}
}
