package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.VertexAttribute;

public interface LayoutItem {

	void provideAttributes(Consumer<VertexAttribute> consumer);

	int getByteWidth();

}
