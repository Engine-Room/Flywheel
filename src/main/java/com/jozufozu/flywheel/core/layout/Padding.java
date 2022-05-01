package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.VertexAttribute;

record Padding(int bytes) implements LayoutItem {

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {

	}

	@Override
	public int getByteWidth() {
		return bytes;
	}

}
