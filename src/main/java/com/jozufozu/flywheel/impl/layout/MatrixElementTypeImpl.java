package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.MatrixElementType;

record MatrixElementTypeImpl(FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns,
							 int byteSize) implements MatrixElementType {
	static MatrixElementTypeImpl create(FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns) {
		if (rows < 2 || rows > 4) {
			throw new IllegalArgumentException("Matrix element row count must be in range [2, 4]!");
		}
		if (columns < 2 || columns > 4) {
			throw new IllegalArgumentException("Matrix element column count must be in range [2, 4]!");
		}
		int byteSize = repr.byteSize() * rows * columns;
		return new MatrixElementTypeImpl(repr, rows, columns, byteSize);
	}
}
