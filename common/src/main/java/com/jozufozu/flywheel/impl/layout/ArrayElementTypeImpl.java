package com.jozufozu.flywheel.impl.layout;

import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.api.layout.ArrayElementType;
import com.jozufozu.flywheel.api.layout.ElementType;

record ArrayElementTypeImpl(ElementType innerType, @Range(from = 1, to = 256) int length, int byteSize, int byteAlignment) implements ArrayElementType {
	static ArrayElementTypeImpl create(ElementType innerType, @Range(from = 1, to = 256) int length) {
		if (length < 1 || length > 256) {
			throw new IllegalArgumentException("Array element length must be in range [1, 256]!");
		}

		return new ArrayElementTypeImpl(innerType, length, innerType.byteSize() * length, innerType.byteAlignment());
	}
}
