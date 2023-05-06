package com.jozufozu.flywheel.lib.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.layout.InputType;
import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

public record MatInput(int rows, int cols, String typeName, String packedTypeName,
					   String unpackingFunction) implements InputType {

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		for (int i = 0; i < rows; i++) {
			consumer.accept(new VertexAttribute.Float(GlNumericType.FLOAT, cols, false));
		}
	}

	@Override
	public int attributeCount() {
		return rows;
	}

	@Override
	public GlslExpr unpack(GlslExpr packed) {
		return packed.callFunction(unpackingFunction);
	}
}
