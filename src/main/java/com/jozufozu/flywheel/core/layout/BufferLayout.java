package com.jozufozu.flywheel.core.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.generate.GlslBuilder;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

import net.minecraft.resources.ResourceLocation;

/**
 * Classic Vertex Format struct with a clever name.
 *
 * <p>
 *     Used for vertices and instances. Describes the layout of a datatype in a buffer object.
 * </p>
 *
 * @see com.jozufozu.flywheel.api.struct.StructType
 * @see VertexType
 */
public class BufferLayout {

	public final List<LayoutItem> layoutItems;
	public final List<VertexAttribute> attributes;

	private final int stride;

	public BufferLayout(List<LayoutItem> layoutItems, int padding) {
		this.layoutItems = ImmutableList.copyOf(layoutItems);

		ImmutableList.Builder<VertexAttribute> attributes = ImmutableList.builder();

		for (var item : layoutItems) {
			item.type.provideAttributes(attributes::add);
		}

		this.attributes = attributes.build();
		this.stride = calculateStride(this.attributes) + padding;
	}

	public List<VertexAttribute> attributes() {
		return attributes;
	}

	public int getAttributeCount() {
		return attributes.size();
	}

	public int getStride() {
		return stride;
	}

	public InstancedArraysComponent getInstancedArraysComponent() {
		return new InstancedArraysComponent();
	}

	public IndirectComponent getIndirectComponent() {
		return new IndirectComponent();
	}

	public static Builder builder() {
		return new Builder();
	}

	private static int calculateStride(List<VertexAttribute> layoutItems) {
		int stride = 0;
		for (var spec : layoutItems) {
			stride += spec.getByteWidth();
		}
		return stride;
	}

	public static class Builder {
		private final ImmutableList.Builder<LayoutItem> allItems;
		private int padding;

		public Builder() {
			allItems = ImmutableList.builder();
		}

		public Builder addItem(InputType type, String name) {
			allItems.add(new LayoutItem(type, name));
			return this;
		}

		public Builder withPadding(int padding) {
			this.padding = padding;
			return this;
		}

		public BufferLayout build() {
			return new BufferLayout(allItems.build(), padding);
		}
	}

	public record LayoutItem(InputType type, String name) {
		public String unpack(String argName) {
			return argName + '.' + name;
		}
	}

	public class InstancedArraysComponent implements SourceComponent {
		@Override
		public Collection<? extends SourceComponent> included() {
			return Collections.emptyList();
		}

		@Override
		public String source(CompilationContext ctx) {
			return generateInstancedArrays(5, "Instance");
		}

		@Override
		public ResourceLocation name() {
			return Flywheel.rl("generated_instanced_arrays");
		}

		public String generateInstancedArrays(int baseIndex, String structName) {
			var builder = new GlslBuilder();
			builder.define("FlwInstance", structName);

			int i = baseIndex;
			final var attributeSuffix = "_vertex_in";
			for (var field : layoutItems) {
				builder.vertexInput()
						.binding(i)
						.type(field.type.typeName())
						.name(field.name + attributeSuffix);

				i += field.type.attributeCount();
			}

			builder.blankLine();

			var structBuilder = builder.struct();
			structBuilder.setName(structName);

			for (var field : layoutItems) {
				structBuilder.addField(field.type.typeName(), field.name);
			}

			builder.blankLine();

			var func = builder.function()
					.returnType(structName)
					.name("flw_unpackInstance");

			var args = layoutItems.stream()
					.map(it -> new GlslExpr.Variable(it.name + attributeSuffix))
					.map(GlslExpr::minPrint)
					.collect(Collectors.joining(", "));

			func.statement("return " + structName + "(" + args + ");");

			return builder.build();
		}
	}

	public class IndirectComponent implements SourceComponent {

		private static final String UNPACK_ARG = "p";

		private static GlslExpr intoGlsl(LayoutItem layoutItem) {
			return GlslExpr.variable(UNPACK_ARG)
					.access(layoutItem.name)
					.transform(layoutItem.type::unpack);
		}

		@Override
		public Collection<? extends SourceComponent> included() {
			return List.of(Components.UTIL_TYPES.getFile());
		}

		@Override
		public ResourceLocation name() {
			return Flywheel.rl("generated_indirect");
		}

		@Override
		public String source(CompilationContext ctx) {
			var content = generateIndirect("IndirectStruct");
			return ctx.generatedHeader(content, name().toString()) + content;
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
				packed.addField(field.type.packedTypeName(), field.name);
				instance.addField(field.type.typeName(), field.name);
			}

			builder.blankLine();

			var func = builder.function()
					.returnType(structName)
					.name("flw_unpackInstance")
					.argumentIn(packedStructName, UNPACK_ARG);

			var args = layoutItems.stream()
					.map(IndirectComponent::intoGlsl)
					.map(GlslExpr::minPrint)
					.collect(Collectors.joining(", "));

			func.statement("return " + structName + "(" + args + ");");

			return builder.build();
		}
	}

}
