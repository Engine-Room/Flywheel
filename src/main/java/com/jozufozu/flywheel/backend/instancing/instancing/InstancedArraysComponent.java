package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.layout.LayoutItem;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class InstancedArraysComponent implements SourceComponent {
	private static final String ATTRIBUTE_SUFFIX = "_vertex_in";

	private final List<LayoutItem> layoutItems;
	private final int baseIndex;

	public InstancedArraysComponent(List<LayoutItem> layoutItems, int baseIndex) {
		this.layoutItems = layoutItems;
		this.baseIndex = baseIndex;
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public String source(CompilationContext ctx) {
		var generated = generateInstancedArrays("Instance");
		return ctx.generatedHeader(generated, name().toString()) + generated;
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("generated_instanced_arrays");
	}

	public String generateInstancedArrays(String structName) {
		var builder = new GlslBuilder();
		builder.define("FlwInstance", structName);

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
		structBuilder.setName(structName);

		for (var field : layoutItems) {
			field.addToStruct(structBuilder);
		}

		builder.blankLine();

		var func = builder.function()
				.returnType(structName)
				.name("flw_unpackInstance");

		var args = layoutItems.stream()
				.map(it -> new GlslExpr.Variable(it.name() + ATTRIBUTE_SUFFIX))
				.map(GlslExpr::minPrint)
				.collect(Collectors.joining(", "));

		func.statement("return " + structName + "(" + args + ");");

		return builder.build();
	}

}
