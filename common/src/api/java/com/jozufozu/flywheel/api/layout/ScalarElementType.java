package com.jozufozu.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public non-sealed interface ScalarElementType extends ElementType {
	ValueRepr repr();
}
