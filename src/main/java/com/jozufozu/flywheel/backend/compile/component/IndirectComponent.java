package com.jozufozu.flywheel.backend.compile.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.backend.compile.LayoutInterpreter;
import com.jozufozu.flywheel.backend.compile.Pipeline;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.layout.LayoutItem;

import net.minecraft.resources.ResourceLocation;

public class IndirectComponent implements SourceComponent {
	private static final String UNPACK_ARG = "p";
	private static final GlslExpr.Variable UNPACKING_VARIABLE = GlslExpr.variable(UNPACK_ARG);
	private static final String STRUCT_NAME = "FlwInstance";
	private static final String PACKED_STRUCT_NAME = "FlwPackedInstance";
	private static final String UNPACK_FN_NAME = "_flw_unpackInstance";

	private final Layout layout;
	private final List<LayoutItem> layoutItems;

	public IndirectComponent(InstanceType<?> type) {
		this.layoutItems = type.oldLayout().layoutItems;
		this.layout = type.layout();
	}

	public static IndirectComponent create(Pipeline.InstanceAssemblerContext ctx) {
		return create(ctx.instanceType());
	}

	public static IndirectComponent create(InstanceType<?> instanceType) {
		return new IndirectComponent(instanceType);
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
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

		generateHeader(builder);

		generateInstanceStruct(builder);

		builder.blankLine();

		generateUnpacking(builder);

		builder.blankLine();

		return builder.build();
	}

	private void generateHeader(GlslBuilder builder) {
		layoutItems.stream()
				.map(LayoutItem::type)
				.distinct()
				.forEach(type -> type.declare(builder));
	}

	private void generateInstanceStruct(GlslBuilder builder) {
		var instance = builder.struct();
		instance.setName(STRUCT_NAME);
		for (var element : layout.elements()) {
			instance.addField(LayoutInterpreter.typeName(element.type()), element.name());
		}
	}

	private void generateUnpacking(GlslBuilder builder) {
		var packed = builder.struct();
		packed.setName(PACKED_STRUCT_NAME);

		var unpackArgs = new ArrayList<GlslExpr>();
		for (LayoutItem field : layoutItems) {
			GlslExpr unpack = UNPACKING_VARIABLE.access(field.name())
					.transform(field.type()::unpack);
			unpackArgs.add(unpack);

			packed.addField(field.type()
					.packedTypeName(), field.name());
		}

		var block = new GlslBlock();
		block.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

		builder.blankLine();
		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg(PACKED_STRUCT_NAME, UNPACK_ARG)
						.build())
				.body(block);
	}
}
