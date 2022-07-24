package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeF;

public class PrimitiveItem implements LayoutItem {

	private final VertexAttribute attribute;

	public PrimitiveItem(GlNumericType type, int count) {
		this(type, count, false);
	}

	public PrimitiveItem(GlNumericType type, int count, boolean normalized) {
		this(new VertexAttributeF(type, count, normalized));
	}

	public PrimitiveItem(VertexAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		consumer.accept(attribute);
	}

}
