package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;

public class PrimitiveItem implements LayoutItem {

	private final VertexAttribute attribute;

	public PrimitiveItem(VertexAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		consumer.accept(attribute);
	}

}
