package com.jozufozu.flywheel.core.layout;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.backend.gl.GlNumericType;

public enum MatrixItems implements LayoutItem {
	MAT3(3, 3),
	MAT4(4, 4),
	;

	private final int rows;
	private final int cols;

	MatrixItems(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}

	@Override
	public void vertexAttribPointer(int stride, int index, int offset) {
		for (int i = 0; i < rows; i++) {
			long attribPointer = offset + (long) i * cols * GlNumericType.FLOAT.getByteWidth();
			GL20.glVertexAttribPointer(index + i, cols, GlNumericType.FLOAT.getGlEnum(), false, stride, attribPointer);
		}
	}

	@Override
	public int size() {
		return GlNumericType.FLOAT.getByteWidth() * rows * cols;
	}

	@Override
	public int attributeCount() {
		return rows;
	}

}
