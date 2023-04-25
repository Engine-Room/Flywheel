package com.jozufozu.flywheel.gl.array;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL45;

import com.jozufozu.flywheel.gl.GlNumericType;

/**
 * A bindable attribute in a vertex array.
 *
 * @param type       The type of the attribute, e.g. GL_FLOAT.
 * @param size       The number of components in the attribute, e.g. 3 for a vec3.
 * @param normalized Whether the data is normalized.
 */
public record VertexAttributeF(GlNumericType type, int size, boolean normalized) implements VertexAttribute {

	@Override
	public int getByteWidth() {
		return size * type.getByteWidth();
	}

	@Override
	public void setup(long offset, int i, int stride) {
		GL32.glVertexAttribPointer(i, size(), type().getGlEnum(), normalized(), stride, offset);
	}

	@Override
	public void setupDSA(int vaobj, int i) {
		GL45.glVertexArrayAttribFormat(vaobj, i, size(), type().getGlEnum(), normalized(), 0);
	}
}
