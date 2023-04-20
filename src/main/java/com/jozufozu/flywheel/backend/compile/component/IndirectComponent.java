package com.jozufozu.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.LayoutItem;
import com.jozufozu.flywheel.backend.compile.Pipeline;
import com.jozufozu.flywheel.backend.compile.Pipelines;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class IndirectComponent implements SourceComponent {
	private static final String UNPACK_ARG = "p";
	private static final GlslExpr.Variable UNPACKING_VARIABLE = GlslExpr.variable(UNPACK_ARG);
	private static final String STRUCT_NAME = "FlwInstance";
	private static final String PACKED_STRUCT_NAME = "FlwPackedInstance";
	private static final String UNPACK_FN_NAME = "_flw_unpackInstance";

	private final List<LayoutItem> layoutItems;
	private final ImmutableList<SourceFile> included;

	public IndirectComponent(Pipeline.InstanceAssemblerContext ctx) {
		this(ctx.sources(), ctx.instanceType());
	}

	public IndirectComponent(ShaderSources sources, InstanceType<?> instanceType) {
		this.layoutItems = instanceType.getLayout().layoutItems;
		included = ImmutableList.of(sources.find(Pipelines.Files.UTIL_TYPES));
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return included;
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("generated_indirect");
	}

	@Override
	public String source() {
		return generateIndirect();
	}

	public String generateIndirect() {
		var builder = new GlslBuilder();

		var instance = builder.struct();
		instance.setName(STRUCT_NAME);
		builder.blankLine();
		var packed = builder.struct();
		packed.setName(PACKED_STRUCT_NAME);

		for (var field : layoutItems) {
			field.addPackedToStruct(packed);
			field.addToStruct(instance);
		}

		builder.blankLine();

		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg(PACKED_STRUCT_NAME, UNPACK_ARG)
						.build())
				.body(this::generateUnpackingBody);

		builder.blankLine();

		return builder.build();
	}

	private void generateUnpackingBody(GlslBlock b) {
		var unpackedFields = layoutItems.stream()
				.map(layoutItem -> layoutItem.unpackField(UNPACKING_VARIABLE))
				.toList();
		b.ret(GlslExpr.call(STRUCT_NAME, unpackedFields));
	}
}
