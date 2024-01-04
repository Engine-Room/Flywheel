package com.jozufozu.flywheel.impl.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.Layout;

final class LayoutImpl implements Layout {
	private final List<Element> elements;
	private final Map<String, ElementType> map;
	private final int byteSize;

	LayoutImpl(List<Element> elements) {
		this.elements = elements;

		map = new HashMap<>();
		int byteSize = 0;
		for (Element element : this.elements) {
			map.put(element.name(), element.type());
			byteSize += element.type().byteSize();
		}
		this.byteSize = byteSize;
	}

	@Override
	@Unmodifiable
	public List<Element> elements() {
		return elements;
	}

	@Override
	@Unmodifiable
	public Map<String, ElementType> asMap() {
		return map;
	}

	@Override
	public int byteSize() {
		return byteSize;
	}

	record ElementImpl(String name, ElementType type) implements Element {
	}
}
