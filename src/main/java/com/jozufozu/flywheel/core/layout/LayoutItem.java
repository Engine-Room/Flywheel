package com.jozufozu.flywheel.core.layout;

import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

public record LayoutItem(InputType type, String name) {
	public GlslExpr unpackField(GlslExpr.Variable struct) {
		return struct.access(name())
				.transform(type()::unpack);
	}

	public void addToStruct(GlslBuilder.StructBuilder structBuilder) {
		structBuilder.addField(type().typeName(), name());
	}

	public void addPackedToStruct(GlslBuilder.StructBuilder packed) {
		packed.addField(type().packedTypeName(), name());
	}
}
