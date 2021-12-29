package com.jozufozu.flywheel.api.shader;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

/**
 * Represents a vertex format agnostic shader.
 */
public interface FlexibleShader<P extends GlProgram> {

	/**
	 * Get a version of this shader that accepts the given VertexType as input.
	 */
	P get(VertexType type);
}
