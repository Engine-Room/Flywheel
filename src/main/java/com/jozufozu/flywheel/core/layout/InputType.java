package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

public interface InputType {

	void provideAttributes(Consumer<VertexAttribute> consumer);

	String typeName();

	String packedTypeName();

	int attributeCount();

	GlslExpr unpack(GlslExpr packed);
}
