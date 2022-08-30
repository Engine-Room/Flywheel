package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeF;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

public record MatrixItem(int rows, int cols, String typeName, String packedTypeName,
						 String unpackingFunction) implements InputType {

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		for (int i = 0; i < rows; i++) {
			consumer.accept(new VertexAttributeF(GlNumericType.FLOAT, cols, false));
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
