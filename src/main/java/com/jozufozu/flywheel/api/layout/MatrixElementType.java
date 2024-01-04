package com.jozufozu.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public non-sealed interface MatrixElementType extends ElementType {
	FloatRepr repr();

	@Range(from = 2, to = 4)
	int rows();

	@Range(from = 2, to = 4)
	int columns();
}
