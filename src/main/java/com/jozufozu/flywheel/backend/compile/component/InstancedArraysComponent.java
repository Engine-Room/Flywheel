package com.jozufozu.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.Collections;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.backend.compile.LayoutInterpreter;
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

	private final Layout layout;
	private final int baseIndex;

	public InstancedArraysComponent(Pipeline.InstanceAssemblerContext ctx) {
		this.layout = ctx.instanceType()
				.layout();
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

		generateVertexInput(builder);

		builder.blankLine();

		var structBuilder = builder.struct();
		structBuilder.setName(STRUCT_NAME);

		for (var element : layout.elements()) {
			structBuilder.addField(LayoutInterpreter.typeName(element.type()), element.name());
		}

		builder.blankLine();

		// unpacking function
		builder.function()
				.signature(FnSignature.of(STRUCT_NAME, UNPACK_FN_NAME))
				.body(this::generateUnpackingBody);

		builder.blankLine();

		return builder.build();
	}

	private void generateVertexInput(GlslBuilder builder) {
		int i = baseIndex;
		for (var element : layout.elements()) {
			var type = element.type();

			builder.vertexInput()
					.binding(i)
					.type(LayoutInterpreter.typeName(type))
					.name(ATTRIBUTE_PREFIX + element.name());

			i += LayoutInterpreter.attributeCount(type);
		}
	}

	private void generateUnpackingBody(GlslBlock b) {
		var fields = layout.elements()
				.stream()
				.map(it -> new GlslExpr.Variable(ATTRIBUTE_PREFIX + it.name()))
				.toList();
		b.ret(GlslExpr.call(STRUCT_NAME, fields));
	}
}
