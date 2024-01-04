package com.jozufozu.flywheel.impl.layout;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.Layout;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

final class LayoutImpl implements Layout {
	@Unmodifiable
	private final List<Element> elements;
	@Unmodifiable
	private final Map<String, ElementType> map;
	private final int byteSize;

	LayoutImpl(@Unmodifiable List<Element> elements) {
		this.elements = elements;

		Object2ObjectOpenHashMap<String, ElementType> map = new Object2ObjectOpenHashMap<>();
		int byteSize = 0;
		for (Element element : this.elements) {
			map.put(element.name(), element.type());
			byteSize += element.type().byteSize();
		}
		map.trim();

		this.map = Collections.unmodifiableMap(map);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elements.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LayoutImpl other = (LayoutImpl) obj;
		return elements.equals(other.elements);
	}

	record ElementImpl(String name, ElementType type) implements Element {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + type.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ElementImpl other = (ElementImpl) obj;
			return name.equals(other.name) && type.equals(other.type);
		}
	}
}
