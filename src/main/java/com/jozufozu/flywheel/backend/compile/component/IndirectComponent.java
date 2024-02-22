package com.jozufozu.flywheel.backend.compile.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.backend.compile.LayoutInterpreter;
import com.jozufozu.flywheel.backend.compile.Pipeline;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.generate.FnSignature;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.backend.glsl.generate.GlslStruct;

import net.minecraft.resources.ResourceLocation;

public class IndirectComponent implements SourceComponent {
	private static final String UNPACK_ARG = "p";
	private static final GlslExpr.Variable UNPACKING_VARIABLE = GlslExpr.variable(UNPACK_ARG);
	private static final String STRUCT_NAME = "FlwInstance";
	private static final String PACKED_STRUCT_NAME = "FlwPackedInstance";
	private static final String UNPACK_FN_NAME = "_flw_unpackInstance";

	private final Layout layout;

	public IndirectComponent(InstanceType<?> type) {
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

		generateInstanceStruct(builder);

		builder.blankLine();

		generateUnpacking(builder);

		builder.blankLine();

		return builder.build();
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

		for (Layout.Element element : layout.elements()) {
			unpackArgs.add(unpackElement(element, packed));
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

	public static GlslExpr unpackElement(Layout.Element element, GlslStruct packed) {
		// FIXME: I don't think we're unpacking signed byte/short values correctly
		// FIXME: we definitely don't consider endianness. this all assumes little endian which works on my machine.
		var type = element.type();
		var name = element.name();

		if (type instanceof ScalarElementType scalar) {
			return unpackScalar(name, packed, scalar);
		} else if (type instanceof VectorElementType vector) {
			return unpackVector(name, packed, vector);
		} else if (type instanceof MatrixElementType matrix) {
			return unpackMatrix(name, packed, matrix);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	private static GlslExpr unpackScalar(String fieldName, GlslStruct packed, ScalarElementType scalar) {
		var repr = scalar.repr();

		if (repr instanceof IntegerRepr intRepr) {
			return unpackIntScalar(fieldName, intRepr, packed);
		} else if (repr instanceof UnsignedIntegerRepr unsignedIntegerRepr) {
			return unpackUnsignedScalar(fieldName, unsignedIntegerRepr, packed);
		} else if (repr instanceof FloatRepr floatRepr) {
			return unpackFloatScalar(fieldName, floatRepr, packed);
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static GlslExpr unpackIntScalar(String fieldName, IntegerRepr intRepr, GlslStruct packed) {
		return switch (intRepr) {
			case BYTE -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFF)
					.cast("int"));
			case SHORT -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFFFF)
					.cast("int"));
			case INT -> unpackScalar(fieldName, packed, "int");
		};
	}

	private static GlslExpr unpackUnsignedScalar(String fieldName, UnsignedIntegerRepr repr, GlslStruct packed) {
		return switch (repr) {
			case UNSIGNED_BYTE -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFF));
			case UNSIGNED_SHORT -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFFFF));
			case UNSIGNED_INT -> unpackScalar(fieldName, packed, "uint");
		};
	}

	private static GlslExpr unpackFloatScalar(String fieldName, FloatRepr repr, GlslStruct packed) {
		return switch (repr) {
			case BYTE -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFF)
					.cast("int")
					.cast("float"));
			case NORMALIZED_BYTE -> unpackScalar(fieldName, packed, "uint", e -> e.callFunction("unpackSnorm4x8")
					.swizzle("x"));
			case UNSIGNED_BYTE -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFF)
					.cast("float"));
			case NORMALIZED_UNSIGNED_BYTE ->
					unpackScalar(fieldName, packed, "uint", e -> e.callFunction("unpackUnorm4x8")
							.swizzle("x"));
			case SHORT -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFFFF)
					.cast("int")
					.cast("float"));
			case NORMALIZED_SHORT -> unpackScalar(fieldName, packed, "uint", e -> e.callFunction("unpackSnorm2x16")
					.swizzle("x"));
			case UNSIGNED_SHORT -> unpackScalar(fieldName, packed, "uint", e -> e.and(0xFFFF)
					.cast("float"));
			case NORMALIZED_UNSIGNED_SHORT ->
					unpackScalar(fieldName, packed, "uint", e -> e.callFunction("unpackUnorm2x16")
							.swizzle("x"));
			case INT -> unpackScalar(fieldName, packed, "int", e -> e.cast("float"));
			case NORMALIZED_INT -> unpackScalar(fieldName, packed, "int", e -> e.div(2147483647f)
					.clamp(-1, 1));
			case UNSIGNED_INT -> unpackScalar(fieldName, packed, "uint", e -> e.cast("float"));
			case NORMALIZED_UNSIGNED_INT -> unpackScalar(fieldName, packed, "uint", e -> e.div(4294967295f));
			case FLOAT -> unpackScalar(fieldName, packed, "float");
		};
	}

	private static GlslExpr unpackScalar(String fieldName, GlslStruct packed, String packedType) {
		return unpackScalar(fieldName, packed, packedType, Function.identity());
	}

	private static GlslExpr unpackScalar(String fieldName, GlslStruct packed, String packedType, Function<GlslExpr, GlslExpr> perElement) {
		packed.addField(packedType, fieldName);
		return perElement.apply(UNPACKING_VARIABLE.access(fieldName));
	}

	private static GlslExpr unpackVector(String fieldName, GlslStruct packed, VectorElementType vector) {
		var repr = vector.repr();

		int size = vector.size();

		if (repr instanceof IntegerRepr intRepr) {
			return unpackIntVector(fieldName, intRepr, packed, size);
		} else if (repr instanceof UnsignedIntegerRepr unsignedIntegerRepr) {
			return unpackUnsignedVector(fieldName, unsignedIntegerRepr, packed, size);
		} else if (repr instanceof FloatRepr floatRepr) {
			return unpackFloatVector(fieldName, floatRepr, packed, size);
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static GlslExpr unpackIntVector(String fieldName, IntegerRepr repr, GlslStruct packed, int size) {
		return switch (repr) {
			case BYTE -> unpackByteBacked(fieldName, packed, size, "ivec" + size, e -> e.cast("int"));
			case SHORT -> unpackShortBacked(fieldName, packed, size, "ivec" + size, e -> e.cast("int"));
			case INT -> unpack(fieldName, packed, size, "int", "ivec" + size);
		};
	}

	private static GlslExpr unpackUnsignedVector(String fieldName, UnsignedIntegerRepr unsignedIntegerRepr, GlslStruct packed, int size) {
		return switch (unsignedIntegerRepr) {
			case UNSIGNED_BYTE -> unpackByteBacked(fieldName, packed, size, "uvec" + size, e -> e.cast("uint"));
			case UNSIGNED_SHORT -> unpackShortBacked(fieldName, packed, size, "uvec" + size, e -> e.cast("uint"));
			case UNSIGNED_INT -> unpack(fieldName, packed, size, "uint", "uvec" + size);
		};
	}

	private static GlslExpr unpackFloatVector(String fieldName, FloatRepr floatRepr, GlslStruct packed, int size) {
		return switch (floatRepr) {
			case NORMALIZED_BYTE -> unpackByteBuiltin(fieldName, packed, size, "unpackSnorm4x8");
			case NORMALIZED_UNSIGNED_BYTE -> unpackByteBuiltin(fieldName, packed, size, "unpackUnorm4x8");
			case NORMALIZED_SHORT -> unpackShortBuiltin(fieldName, packed, size, "unpackSnorm2x16");
			case NORMALIZED_UNSIGNED_SHORT -> unpackShortBuiltin(fieldName, packed, size, "unpackUnorm2x16");
			case NORMALIZED_INT -> unpack(fieldName, packed, size, "int", "vec" + size, e -> e.div(2147483647f)
					.clamp(-1, 1));
			case NORMALIZED_UNSIGNED_INT ->
					unpack(fieldName, packed, size, "uint", "vec" + size, e -> e.div(4294967295f));
			case BYTE -> unpackByteBacked(fieldName, packed, size, "vec" + size, e -> e.cast("int")
					.cast("float"));
			case UNSIGNED_BYTE -> unpackByteBacked(fieldName, packed, size, "vec" + size, e -> e.cast("float"));
			case SHORT -> unpackShortBacked(fieldName, packed, size, "vec" + size, e -> e.cast("int")
					.cast("float"));
			case UNSIGNED_SHORT -> unpackShortBacked(fieldName, packed, size, "vec" + size, e -> e.cast("float"));
			case INT -> unpack(fieldName, packed, size, "int", "vec" + size, e -> e.cast("float"));
			case UNSIGNED_INT -> unpack(fieldName, packed, size, "float", "vec" + size, e -> e.cast("float"));
			case FLOAT -> unpack(fieldName, packed, size, "float", "vec" + size);
		};
	}

	private static GlslExpr unpackByteBacked(String fieldName, GlslStruct packed, int size, String outType, Function<GlslExpr, GlslExpr> perElement) {
		packed.addField("uint", fieldName);
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int bitPos = i * 8;
			var element = UNPACKING_VARIABLE.access(fieldName)
					.and(0xFF << bitPos)
					.rsh(bitPos);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackShortBacked(String fieldName, GlslStruct packed, int size, String outType, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int unpackField = i / 2;
			int bitPos = (i % 2) * 16;
			var name = fieldName + "_" + unpackField;
			if (bitPos == 0) {
				// First time we're seeing this field, add it to the struct.
				packed.addField("uint", name);
			}
			var element = UNPACKING_VARIABLE.access(name)
					.and(0xFFFF << bitPos)
					.rsh(bitPos);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpack(String fieldName, GlslStruct packed, int size, String backingType, String outType) {
		return unpack(fieldName, packed, size, backingType, outType, Function.identity());
	}

	private static GlslExpr unpack(String fieldName, GlslStruct packed, int size, String backingType, String outType, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			var name = fieldName + "_" + i;
			packed.addField(backingType, name);
			args.add(UNPACKING_VARIABLE.access(name)
					.transform(perElement));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackByteBuiltin(String fieldName, GlslStruct packed, int size, String func) {
		packed.addField("uint", fieldName);
		GlslExpr expr = UNPACKING_VARIABLE.access(fieldName)
				.callFunction(func);
		return switch (size) {
			case 2 -> expr.swizzle("xy");
			case 3 -> expr.swizzle("xyz");
			case 4 -> expr;
			default -> throw new IllegalArgumentException("Invalid vector size " + size);
		};
	}

	private static GlslExpr unpackShortBuiltin(String fieldName, GlslStruct packed, int size, String func) {
		if (size == 2) {
			packed.addField("uint", fieldName);
			return UNPACKING_VARIABLE.access(fieldName)
					.callFunction(func);
		} else {
			var name0 = fieldName + "_" + 0;
			var name1 = fieldName + "_" + 1;
			packed.addField("uint", name0);
			packed.addField("uint", name1);
			GlslExpr xy = UNPACKING_VARIABLE.access(name0)
					.callFunction(func);

			GlslExpr zw = UNPACKING_VARIABLE.access(name1)
					.callFunction(func);

			if (size == 3) {
				return GlslExpr.call("vec3", List.of(xy.swizzle("xy"), zw.swizzle("x")));
			} else {
				return GlslExpr.call("vec4", List.of(xy, zw));
			}
		}
	}

	private static GlslExpr unpackMatrix(String name, GlslStruct packed, MatrixElementType matrix) {
		var repr = matrix.repr();

		int rows = matrix.rows();
		int columns = matrix.columns();

		List<GlslExpr> args = new ArrayList<>();

		for (int i = 0; i < columns; i++) {
			args.add(unpackFloatVector(name + "_" + i, repr, packed, rows));
		}

		return GlslExpr.call("mat" + columns + "x" + rows, args);
	}
}
