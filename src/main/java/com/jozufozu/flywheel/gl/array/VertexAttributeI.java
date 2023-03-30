package com.jozufozu.flywheel.gl.array;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL45;

import com.jozufozu.flywheel.gl.GlNumericType;

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

	@Override
	public void format(int vaobj, int i) {
		GL45.glVertexArrayAttribIFormat(vaobj, i, size(), type().getGlEnum(), 0);
	}
}
