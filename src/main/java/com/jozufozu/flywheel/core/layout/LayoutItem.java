package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;

public interface LayoutItem {

	void provideAttributes(Consumer<VertexAttribute> consumer);

}
