package dev.engine_room.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

import dev.engine_room.flywheel.api.internal.FlwApiLink;

@ApiStatus.NonExtendable
public interface LayoutBuilder {
	LayoutBuilder scalar(String name, ValueRepr repr);

	LayoutBuilder vector(String name, ValueRepr repr, @Range(from = 2, to = 4) int size);

	LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns);

	LayoutBuilder matrix(String name, FloatRepr repr, @Range(from = 2, to = 4) int size);

	LayoutBuilder scalarArray(String name, ValueRepr repr, @Range(from = 1, to = 256) int length);

	LayoutBuilder vectorArray(String name, ValueRepr repr, @Range(from = 2, to = 4) int size, @Range(from = 1, to = 256) int length);

	LayoutBuilder matrixArray(String name, FloatRepr repr, @Range(from = 2, to = 4) int rows, @Range(from = 2, to = 4) int columns, @Range(from = 1, to = 256) int length);

	LayoutBuilder matrixArray(String name, FloatRepr repr, @Range(from = 2, to = 4) int size, @Range(from = 1, to = 256) int length);

	Layout build();

	static LayoutBuilder create() {
		return FlwApiLink.INSTANCE.createLayoutBuilder();
	}
}
