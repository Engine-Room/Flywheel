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
import com.jozufozu.flywheel.backend.glsl.generate.GlslStmt;
import com.jozufozu.flywheel.lib.math.MoreMath;

import net.minecraft.resources.ResourceLocation;

public class SamplerBufferComponent implements SourceComponent {
	private static final String STRUCT_NAME = "FlwInstance";
	private static final String UNPACK_FN_NAME = "_flw_unpackInstance";
	private static final String UNPACK_ARG = "index";

	private final Layout layout;

	public SamplerBufferComponent(InstanceType<?> type) {
		this.layout = type.layout();
	}

	public static SamplerBufferComponent create(Pipeline.InstanceAssemblerContext ctx) {
		return create(ctx.instanceType());
	}

	public static SamplerBufferComponent create(InstanceType<?> instanceType) {
		return new SamplerBufferComponent(instanceType);
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public ResourceLocation name() {
		return Flywheel.rl("generated_instancing");
	}

	@Override
	public String source() {
		return generateIndirect();
	}

	public String generateIndirect() {
		var builder = new GlslBuilder();

		generateInstanceStruct(builder);

		builder.blankLine();

		builder._addRaw("uniform usamplerBuffer _flw_instances;");

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
		var block = new GlslBlock();

		var texels = MoreMath.ceilingDiv(layout.byteSize(), 16);

		block.add(GlslStmt.raw("int base = " + UNPACK_ARG + " * " + texels + ";"));

		for (int i = 0; i < texels; i++) {
			// Fetch all the texels for the given instance ahead of time to simplify the unpacking generators.
			block.add(GlslStmt.raw("uvec4 u" + i + " = texelFetch(_flw_instances, base + " + i + ");"));
		}

		var unpackArgs = new ArrayList<GlslExpr>();
		int uintOffset = 0;
		for (Layout.Element element : layout.elements()) {
			unpackArgs.add(unpackElement(element, uintOffset));
			uintOffset += MoreMath.ceilingDiv(element.type().byteSize(), 4);
		}

		block.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg("int", UNPACK_ARG)
						.build())
				.body(block);
	}

	public static GlslExpr access(int uintOffset) {
		return GlslExpr.variable("u" + (uintOffset >> 2))
				.swizzle(String.valueOf("xyzw".charAt(uintOffset & 3)));
	}

	// TODO: deduplicate this with IndirectComponent somehow?

	public static GlslExpr unpackElement(Layout.Element element, int uintOffset) {
		// FIXME: I don't think we're unpacking signed byte/short values correctly
		// FIXME: we definitely don't consider endianness. this all assumes little endian which works on my machine.
		var type = element.type();

		if (type instanceof ScalarElementType scalar) {
			return unpackScalar(uintOffset, scalar);
		} else if (type instanceof VectorElementType vector) {
			return unpackVector(uintOffset, vector);
		} else if (type instanceof MatrixElementType matrix) {
			return unpackMatrix(uintOffset, matrix);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	private static GlslExpr unpackScalar(int uintOffset, ScalarElementType scalar) {
		var repr = scalar.repr();

		if (repr instanceof IntegerRepr intRepr) {
			return unpackIntScalar(uintOffset, intRepr);
		} else if (repr instanceof UnsignedIntegerRepr unsignedIntegerRepr) {
			return unpackUnsignedScalar(uintOffset, unsignedIntegerRepr);
		} else if (repr instanceof FloatRepr floatRepr) {
			return unpackFloatScalar(uintOffset, floatRepr);
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static GlslExpr unpackIntScalar(int uintOffset, IntegerRepr intRepr) {
		return switch (intRepr) {
			case BYTE -> unpackScalar(uintOffset, e -> e.and(0xFF)
					.cast("int"));
			case SHORT -> unpackScalar(uintOffset, e -> e.and(0xFFFF)
					.cast("int"));
			case INT -> unpackScalar(uintOffset);
		};
	}

	private static GlslExpr unpackUnsignedScalar(int uintOffset, UnsignedIntegerRepr repr) {
		return switch (repr) {
			case UNSIGNED_BYTE -> unpackScalar(uintOffset, e -> e.and(0xFF));
			case UNSIGNED_SHORT -> unpackScalar(uintOffset, e -> e.and(0xFFFF));
			case UNSIGNED_INT -> unpackScalar(uintOffset);
		};
	}

	private static GlslExpr unpackFloatScalar(int uintOffset, FloatRepr repr) {
		return switch (repr) {
			case BYTE -> unpackScalar(uintOffset, e -> e.and(0xFF)
					.cast("int")
					.cast("float"));
			case NORMALIZED_BYTE -> unpackScalar(uintOffset, e -> e.callFunction("unpackSnorm4x8")
					.swizzle("x"));
			case UNSIGNED_BYTE -> unpackScalar(uintOffset, e -> e.and(0xFF)
					.cast("float"));
			case NORMALIZED_UNSIGNED_BYTE ->
					unpackScalar(uintOffset, e -> e.callFunction("unpackUnorm4x8")
							.swizzle("x"));
			case SHORT -> unpackScalar(uintOffset, e -> e.and(0xFFFF)
					.cast("int")
					.cast("float"));
			case NORMALIZED_SHORT -> unpackScalar(uintOffset, e -> e.callFunction("unpackSnorm2x16")
					.swizzle("x"));
			case UNSIGNED_SHORT -> unpackScalar(uintOffset, e -> e.and(0xFFFF)
					.cast("float"));
			case NORMALIZED_UNSIGNED_SHORT ->
					unpackScalar(uintOffset, e -> e.callFunction("unpackUnorm2x16")
							.swizzle("x"));
			case INT -> unpackScalar(uintOffset, e -> e.cast("int").cast("float"));
			case NORMALIZED_INT -> unpackScalar(uintOffset, e -> e.div(2147483647f)
					.clamp(-1, 1));
			case UNSIGNED_INT -> unpackScalar(uintOffset, e -> e.cast("float"));
			case NORMALIZED_UNSIGNED_INT -> unpackScalar(uintOffset, e -> e.div(4294967295f));
			case FLOAT -> unpackScalar(uintOffset, e -> e.callFunction("uintBitsToFloat"));
		};
	}

	private static GlslExpr unpackScalar(int uintOffset) {
		return unpackScalar(uintOffset, Function.identity());
	}

	private static GlslExpr unpackScalar(int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		return perElement.apply(access(uintOffset));
	}

	private static GlslExpr unpackVector(int uintOffset, VectorElementType vector) {
		var repr = vector.repr();

		int size = vector.size();

		if (repr instanceof IntegerRepr intRepr) {
			return unpackIntVector(uintOffset, intRepr, size);
		} else if (repr instanceof UnsignedIntegerRepr unsignedIntegerRepr) {
			return unpackUnsignedVector(uintOffset, unsignedIntegerRepr, size);
		} else if (repr instanceof FloatRepr floatRepr) {
			return unpackFloatVector(uintOffset, floatRepr, size);
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	private static GlslExpr unpackIntVector(int uintOffset, IntegerRepr repr, int size) {
		return switch (repr) {
			case BYTE -> unpackByteBacked(uintOffset, size, "ivec" + size, e -> e.cast("int"));
			case SHORT -> unpackShortBacked(uintOffset, size, "ivec" + size, e -> e.cast("int"));
			case INT -> unpack(uintOffset, size, "ivec" + size);
		};
	}

	private static GlslExpr unpackUnsignedVector(int uintOffset, UnsignedIntegerRepr unsignedIntegerRepr, int size) {
		return switch (unsignedIntegerRepr) {
			case UNSIGNED_BYTE -> unpackByteBacked(uintOffset, size, "uvec" + size, Function.identity());
			case UNSIGNED_SHORT -> unpackShortBacked(uintOffset, size, "uvec" + size, Function.identity());
			case UNSIGNED_INT -> unpack(uintOffset, size, "uvec" + size);
		};
	}

	private static GlslExpr unpackFloatVector(int uintOffset, FloatRepr floatRepr, int size) {
		return switch (floatRepr) {
			case NORMALIZED_BYTE -> unpackByteBacked(uintOffset, size, "vec" + size, e -> e.div(127).clamp(-1, 1));
			case NORMALIZED_UNSIGNED_BYTE -> unpackByteBacked(uintOffset, size, "vec" + size, e -> e.div(255));
			case NORMALIZED_SHORT -> unpackShortBacked(uintOffset, size, "vec" + size, e -> e.div(32727).clamp(-1, 1));
			case NORMALIZED_UNSIGNED_SHORT -> unpackShortBacked(uintOffset, size, "vec" + size, e -> e.div(65535));
			case NORMALIZED_INT -> unpack(uintOffset, size, "vec" + size, e -> e.div(2147483647f)
					.clamp(-1, 1));
			case NORMALIZED_UNSIGNED_INT ->
					unpack(uintOffset, size, "vec" + size, e -> e.div(4294967295f));
			case BYTE -> unpackByteBacked(uintOffset, size, "vec" + size, e -> e.cast("int")
					.cast("float"));
			case UNSIGNED_BYTE -> unpackByteBacked(uintOffset, size, "vec" + size, e -> e.cast("float"));
			case SHORT -> unpackShortBacked(uintOffset, size, "vec" + size, e -> e.cast("int")
					.cast("float"));
			case UNSIGNED_SHORT -> unpackShortBacked(uintOffset, size, "vec" + size, e -> e.cast("float"));
			case INT -> unpack(uintOffset, size, "vec" + size, e -> e.cast("float"));
			case UNSIGNED_INT -> unpack(uintOffset, size, "vec" + size, e -> e.cast("int").cast("float"));
			case FLOAT -> unpack(uintOffset, size, "vec" + size, e -> e.callFunction("uintBitsToFloat"));
		};
	}

	private static GlslExpr unpackByteBacked(int uintOffset, int size, String outType, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int bitPos = i * 8;
			var element = access(uintOffset)
					.and(0xFF << bitPos)
					.rsh(bitPos);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackShortBacked(int uintOffset, int size, String outType, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int bitPos = (i % 2) * 16;
			int wordOffset = i / 2;
			var element = access(uintOffset + wordOffset)
					.and(0xFFFF << bitPos)
					.rsh(bitPos);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpack(int uintOffset, int size, String outType) {
		return unpack(uintOffset, size, outType, Function.identity());
	}

	private static GlslExpr unpack(int uintOffset, int size, String outType, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			args.add(access(uintOffset + i)
					.transform(perElement));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackMatrix(int uintOffset, MatrixElementType matrix) {
		var repr = matrix.repr();

		int rows = matrix.rows();
		int columns = matrix.columns();

		int columnWordSize = MoreMath.ceilingDiv(rows * repr.byteSize(), 4);

		List<GlslExpr> args = new ArrayList<>();

		for (int i = 0; i < columns; i++) {
			args.add(unpackFloatVector(uintOffset + i * columnWordSize, repr, rows));
		}

		return GlslExpr.call("mat" + columns + "x" + rows, args);
	}
}
