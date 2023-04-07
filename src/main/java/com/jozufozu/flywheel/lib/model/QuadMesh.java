package com.jozufozu.flywheel.lib.model;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.gl.buffer.ElementBuffer;
import com.jozufozu.flywheel.lib.util.QuadConverter;

public interface QuadMesh extends Mesh {
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
	@Override
	default ElementBuffer createEBO() {
		return QuadConverter.getInstance()
				.quads2Tris(getVertexCount() / 4);
	}
}
