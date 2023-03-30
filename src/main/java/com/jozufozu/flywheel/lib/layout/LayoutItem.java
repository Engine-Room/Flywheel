package com.jozufozu.flywheel.lib.layout;

import com.jozufozu.flywheel.api.layout.InputType;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.glsl.generate.GlslStruct;

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
