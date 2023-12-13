package com.jozufozu.flywheel.lib.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

public interface InputType {

	void provideAttributes(Consumer<VertexAttribute> consumer);

	String typeName();

	String packedTypeName();

	int attributeCount();

	GlslExpr unpack(GlslExpr packed);

	void declare(GlslBuilder builder);
}
