package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.VertexAttribute;

public class PrimitiveItem implements LayoutItem {

	private final VertexAttribute attribute;

	public PrimitiveItem(GlNumericType type, int count) {
		this(type, count, false);
	}

	public PrimitiveItem(GlNumericType type, int count, boolean normalized) {
		attribute = new VertexAttribute(count, type, normalized);
	}

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		consumer.accept(attribute);
	}

}
