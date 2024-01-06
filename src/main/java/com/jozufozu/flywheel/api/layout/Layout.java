package com.jozufozu.flywheel.api.layout;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

@ApiStatus.NonExtendable
public interface Layout {
	int MAX_ELEMENT_NAME_LENGTH = 896;

	@Unmodifiable
	List<Element> elements();

	@Unmodifiable Map<String, Element> asMap();

	int byteSize();

	@ApiStatus.NonExtendable
	interface Element {
		String name();

		ElementType type();

		int offset();

		int byteSize();
	}
}
