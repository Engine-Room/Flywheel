package com.jozufozu.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.layout.LayoutItem;
import com.jozufozu.flywheel.backend.compile.Pipeline;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class InstancedArraysComponent implements SourceComponent {
	private static final String ATTRIBUTE_PREFIX = "_flw_i_";
	private static final String STRUCT_NAME = "FlwInstance";
	private static final String UNPACK_FN_NAME = "_flw_unpackInstance";

	private final List<LayoutItem> layoutItems;
	private final int baseIndex;

	public InstancedArraysComponent(Pipeline.InstanceAssemblerContext ctx) {
		this.layoutItems = ctx.instanceType()
				.getLayout().layoutItems;
		this.baseIndex = ctx.baseAttribute();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("generated_instanced_arrays");
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		int i = baseIndex;
		for (var field : layoutItems) {
			builder.vertexInput()
					.binding(i)
					.type(field.type()
							.typeName())
					.name(ATTRIBUTE_PREFIX + field.name());

			i += field.type()
					.attributeCount();
		}

		builder.blankLine();

		var structBuilder = builder.struct();
		structBuilder.setName(STRUCT_NAME);

		for (var field : layoutItems) {
			structBuilder.addField(field.type()
					.typeName(), field.name());
		}

		builder.blankLine();

		// unpacking function
		builder.function()
				.signature(FnSignature.of(STRUCT_NAME, UNPACK_FN_NAME))
				.body(this::generateUnpackingBody);

		builder.blankLine();

		return builder.build();
	}

	private void generateUnpackingBody(GlslBlock b) {
		var fields = layoutItems.stream()
				.map(it -> new GlslExpr.Variable(ATTRIBUTE_PREFIX + it.name()))
				.toList();
		b.ret(GlslExpr.call(STRUCT_NAME, fields));
	}
}
