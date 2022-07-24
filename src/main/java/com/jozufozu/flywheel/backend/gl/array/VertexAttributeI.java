package com.jozufozu.flywheel.backend.gl.array;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.backend.gl.GlNumericType;

/**
 * A bindable attribute in a vertex array.
 *
 * @param type The type of the attribute, e.g. GL_INT.
 * @param size The number of components in the attribute, e.g. 3 for a vec3.
 */
public record VertexAttributeI(GlNumericType type, int size) implements VertexAttribute {

	@Override
	public int getByteWidth() {
		return size * type.getByteWidth();
	}

	@Override
	public void pointer(long offset, int i, int stride) {
		GL32.glVertexAttribIPointer(i, size(), type().getGlEnum(), stride, offset);
	}
}
