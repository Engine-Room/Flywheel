package com.jozufozu.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

import com.jozufozu.flywheel.impl.layout.LayoutBuilderImpl;

@ApiStatus.NonExtendable
public interface LayoutBuilder {
	LayoutBuilder scalar(String name, ValueRepr repr);

	LayoutBuilder vector(String name, ValueRepr repr, @Range(from = 2, to = 4) int size);

	LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns);

	LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int size);

	Layout build();

	static LayoutBuilder create() {
		return new LayoutBuilderImpl();
	}
}
