package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.layout.LayoutItem;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class InstancedArraysComponent implements SourceComponent {
	private static final String ATTRIBUTE_SUFFIX = "_vertex_in";
	private static final String STRUCT_NAME = "Instance";

	private final List<LayoutItem> layoutItems;
	private final int baseIndex;

	public InstancedArraysComponent(Pipeline.InstanceAssemblerContext ctx) {
		this.layoutItems = ctx.structType()
				.getLayout().layoutItems;
		this.baseIndex = ctx.vertexType()
				.getLayout()
				.getAttributeCount();
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
		builder.define("FlwInstance", STRUCT_NAME);

		int i = baseIndex;
		for (var field : layoutItems) {
			builder.vertexInput()
					.binding(i)
					.type(field.type()
							.typeName())
					.name(field.name() + ATTRIBUTE_SUFFIX);

			i += field.type()
					.attributeCount();
		}

		builder.blankLine();

		var structBuilder = builder.struct();
		structBuilder.setName(STRUCT_NAME);

		for (var field : layoutItems) {
			field.addToStruct(structBuilder);
		}

		builder.blankLine();

		// unpacking function
		builder.function()
				.signature(FnSignature.of(STRUCT_NAME, "flw_unpackInstance"))
				.body(this::generateUnpackingBody);

		return builder.build();
	}

	private void generateUnpackingBody(GlslBlock b) {
		var fields = layoutItems.stream()
				.map(it -> new GlslExpr.Variable(it.name() + ATTRIBUTE_SUFFIX))
				.toList();
		b.ret(GlslExpr.call(STRUCT_NAME, fields));
	}
}
