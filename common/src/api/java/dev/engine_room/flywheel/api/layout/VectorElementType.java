package com.jozufozu.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public non-sealed interface VectorElementType extends ElementType {
	ValueRepr repr();

	@Range(from = 2, to = 4)
	int size();
}
