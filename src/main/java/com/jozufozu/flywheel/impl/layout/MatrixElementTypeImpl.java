package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.MatrixElementType;

final class MatrixElementTypeImpl implements MatrixElementType {
	private final FloatRepr repr;
	@Range(from = 2, to = 4)
	private final int rows;
	@Range(from = 2, to = 4)
	private final int columns;
	private final int byteSize;

	MatrixElementTypeImpl(FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns) {
		if (rows < 2 || rows > 4) {
			throw new IllegalArgumentException("Matrix element row count must be in range [2, 4]!");
		}
		if (columns < 2 || columns > 4) {
			throw new IllegalArgumentException("Matrix element column count must be in range [2, 4]!");
		}

		this.repr = repr;
		this.rows = rows;
		this.columns = columns;
		byteSize = repr.byteSize() * rows * columns;
	}

	@Override
	public FloatRepr repr() {
		return repr;
	}

	@Override
	@Range(from = 2, to = 4)
	public int rows() {
		return rows;
	}

	@Override
	@Range(from = 2, to = 4)
	public int columns() {
		return columns;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + repr.hashCode();
		result = prime * result + rows;
		result = prime * result + columns;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MatrixElementTypeImpl other = (MatrixElementTypeImpl) obj;
		return repr == other.repr && rows == other.rows && columns == other.columns;
	}
}
