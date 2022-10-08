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
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

public class IndirectComponent implements SourceComponent {

	private static final String UNPACK_ARG = "p";
	private static final GlslExpr.Variable UNPACKING_VARIABLE = GlslExpr.variable(UNPACK_ARG);

	private final List<LayoutItem> layoutItems;
	private final ImmutableList<SourceFile> included;

	public IndirectComponent(Pipeline.InstanceAssemblerContext ctx) {
		this(ctx.sources(), ctx.structType());
	}

	public IndirectComponent(ShaderSources sources, StructType<?> structType) {
		this.layoutItems = structType.getLayout().layoutItems;
		included = ImmutableList.of(sources.find(Pipelines.Files.UTIL_TYPES.resourceLocation()));
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
		return generateIndirect("IndirectStruct");
	}

	public String generateIndirect(String structName) {
		var builder = new GlslBuilder();
		final var packedStructName = structName + "_packed";
		builder.define("FlwInstance", structName);
		builder.define("FlwPackedInstance", packedStructName);

		var packed = builder.struct();
		builder.blankLine();
		var instance = builder.struct();
		packed.setName(packedStructName);
		instance.setName(structName);

		for (var field : layoutItems) {
			field.addPackedToStruct(packed);
			field.addToStruct(instance);
		}

		builder.blankLine();

		builder.function()
				.returnType(structName)
				.name("flw_unpackInstance")
				.argumentIn(packedStructName, UNPACK_ARG)
				.body(b -> b.ret(GlslExpr.call(structName, layoutItems.stream()
						.map(layoutItem -> layoutItem.unpackField(UNPACKING_VARIABLE))
						.toList())));

		return builder.build();
	}
}
