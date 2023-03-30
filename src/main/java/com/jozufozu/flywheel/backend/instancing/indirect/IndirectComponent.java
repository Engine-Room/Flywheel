package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.Pipelines;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.layout.LayoutItem;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class IndirectComponent implements SourceComponent {

	private static final String UNPACK_ARG = "p";
	private static final GlslExpr.Variable UNPACKING_VARIABLE = GlslExpr.variable(UNPACK_ARG);
	private static final String STRUCT_NAME = "IndirectStruct";
	private static final String PACKED_STRUCT_NAME = STRUCT_NAME + "_packed";

	private final List<LayoutItem> layoutItems;
	private final ImmutableList<SourceFile> included;

	public IndirectComponent(Pipeline.InstanceAssemblerContext ctx) {
		this(ctx.sources(), ctx.structType());
	}

	public IndirectComponent(ShaderSources sources, StructType<?> structType) {
		this.layoutItems = structType.getLayout().layoutItems;
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
		builder.define("FlwInstance", STRUCT_NAME);
		builder.define("FlwPackedInstance", PACKED_STRUCT_NAME);

		var packed = builder.struct();
		builder.blankLine();
		var instance = builder.struct();
		packed.setName(PACKED_STRUCT_NAME);
		instance.setName(STRUCT_NAME);

		for (var field : layoutItems) {
			field.addPackedToStruct(packed);
			field.addToStruct(instance);
		}

		builder.blankLine();

		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name("flw_unpackInstance")
						.arg(PACKED_STRUCT_NAME, UNPACK_ARG)
						.build())
				.body(this::generateUnpackingBody);

		return builder.build();
	}

	private void generateUnpackingBody(GlslBlock b) {
		var unpackedFields = layoutItems.stream()
				.map(layoutItem -> layoutItem.unpackField(UNPACKING_VARIABLE))
				.toList();
		b.ret(GlslExpr.call(STRUCT_NAME, unpackedFields));
	}
}
