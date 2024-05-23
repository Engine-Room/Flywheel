package dev.engine_room.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.MatrixElementType;

record MatrixElementTypeImpl(FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns,
							 int byteSize, int byteAlignment) implements MatrixElementType {
	static MatrixElementTypeImpl create(FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns) {
		if (rows < 2 || rows > 4) {
			throw new IllegalArgumentException("Matrix element row count must be in range [2, 4]!");
		}
		if (columns < 2 || columns > 4) {
			throw new IllegalArgumentException("Matrix element column count must be in range [2, 4]!");
		}

		return new MatrixElementTypeImpl(repr, rows, columns, repr.byteSize() * rows * columns, repr.byteSize());
	}
}
