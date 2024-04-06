package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;

record VectorElementTypeImpl(ValueRepr repr, @Range(from = 2, to = 4) int size,
							 int byteSize, int byteAlignment) implements VectorElementType {
	static VectorElementTypeImpl create(ValueRepr repr, @Range(from = 2, to = 4) int size) {
		if (size < 2 || size > 4) {
			throw new IllegalArgumentException("Vector element size must be in range [2, 4]!");
		}

		return new VectorElementTypeImpl(repr, size, repr.byteSize() * size, repr.byteSize());
	}
}
