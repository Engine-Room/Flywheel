package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.lib.math.MoreMath;

record VectorElementTypeImpl(ValueRepr repr, @Range(from = 2, to = 4) int size,
							 int byteSize) implements VectorElementType {

	static VectorElementTypeImpl create(ValueRepr repr, @Range(from = 2, to = 4) int size) {
		if (size < 2 || size > 4) {
			throw new IllegalArgumentException("Vector element size must be in range [2, 4]!");
		}

		int byteSize = MoreMath.align4(repr.byteSize() * size);
		return new VectorElementTypeImpl(repr, size, byteSize);
	}
}
