package com.jozufozu.flywheel.lib.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.layout.InputType;
import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
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

	@Override
	public void declare(GlslBuilder builder) {

		var s = builder.struct();
		s.setName(packedTypeName);

		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				s.addField("float", "m" + c + r);
			}
		}

		builder.function()
				.signature(FnSignature.create()
						.name(unpackingFunction)
						.returnType(typeName)
						.arg(packedTypeName, "p")
						.build())
				.body(block -> {
					List<GlslExpr> args = new ArrayList<>();
					var p = GlslExpr.variable("p");

					for (int c = 0; c < cols; c++) {
						for (int r = 0; r < rows; r++) {
							args.add(p.access("m" + c + r));
						}
					}

					block.ret(GlslExpr.call(typeName, args));
				});
	}
}
