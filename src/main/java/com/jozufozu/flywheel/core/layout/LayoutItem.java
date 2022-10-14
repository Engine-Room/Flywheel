package com.jozufozu.flywheel.core.layout;

import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.core.source.generate.GlslStruct;

public record LayoutItem(InputType type, String name) {
	public GlslExpr unpackField(GlslExpr.Variable struct) {
		return struct.access(name())
				.transform(type()::unpack);
	}

	public void addToStruct(GlslStruct glslStruct) {
		glslStruct.addField(type().typeName(), name());
	}

	public void addPackedToStruct(GlslStruct packed) {
		packed.addField(type().packedTypeName(), name());
	}
}
