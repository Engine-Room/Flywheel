package com.jozufozu.flywheel.api.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

public interface InputType {

	void provideAttributes(Consumer<VertexAttribute> consumer);

	String typeName();

	String packedTypeName();

	int attributeCount();

	GlslExpr unpack(GlslExpr packed);
}
