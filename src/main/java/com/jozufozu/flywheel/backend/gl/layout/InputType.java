package com.jozufozu.flywheel.backend.gl.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;

public interface InputType {

	void provideAttributes(Consumer<VertexAttribute> consumer);

	String typeName();

	String packedTypeName();

	int attributeCount();

	GlslExpr unpack(GlslExpr packed);

	void declare(GlslBuilder builder);
}
