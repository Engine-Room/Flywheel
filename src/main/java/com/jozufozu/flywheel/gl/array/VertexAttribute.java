package com.jozufozu.flywheel.gl.array;

import com.jozufozu.flywheel.gl.GlNumericType;

public sealed interface VertexAttribute {
	int byteWidth();

	/**
	 * A bindable attribute in a vertex array.
	 *
	 * @param type       The type of the attribute, e.g. GL_FLOAT.
	 * @param size       The number of components in the attribute, e.g. 3 for a vec3.
	 * @param normalized Whether the data is normalized.
	 */
	record Float(GlNumericType type, int size, boolean normalized) implements VertexAttribute {
		@Override
		public int byteWidth() {
			return size * type.byteWidth();
		}
	}

	/**
	 * A bindable attribute in a vertex array.
	 *
	 * @param type The type of the attribute, e.g. GL_INT.
	 * @param size The number of components in the attribute, e.g. 3 for a vec3.
	 */
	record Int(GlNumericType type, int size) implements VertexAttribute {
		@Override
		public int byteWidth() {
			return size * type.byteWidth();
		}
	}
}
