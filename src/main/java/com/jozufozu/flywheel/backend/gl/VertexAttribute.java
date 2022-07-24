package com.jozufozu.flywheel.backend.gl;

// TODO: support glVertexAttribIPointer
/**
 * A bindable attribute in a vertex array.
 *
 * @param size The number of components in the attribute, e.g. 3 for a vec3.
 * @param type The type of the attribute, e.g. GL_FLOAT.
 * @param normalized Whether the data is normalized.
 */
public record VertexAttribute(int size, GlNumericType type, boolean normalized) {

	public int getByteWidth() {
		return size * type.getByteWidth();
	}
}
