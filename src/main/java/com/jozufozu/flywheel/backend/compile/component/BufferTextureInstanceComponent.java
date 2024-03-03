package com.jozufozu.flywheel.backend.compile.component;

import java.util.ArrayList;
import java.util.EnumMap;
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
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.backend.compile.Pipeline;
import com.jozufozu.flywheel.backend.glsl.generate.FnSignature;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.backend.glsl.generate.GlslStmt;
import com.jozufozu.flywheel.lib.math.MoreMath;

public class BufferTextureInstanceComponent extends InstanceAssemblerComponent {
	private static final String UNPACK_ARG = "index";

	private static final String[] SWIZZLE_SELECTORS = { "x", "y", "z", "w" };

	// Each function receives a uint expression as the input.
	// For byte unpacking, the lowest 8 bits contain the value. For short unpacking, the lowest 16 bits contain the value.
	// In both cases, all other bits are 0.
	private static final EnumMap<IntegerRepr, Function<GlslExpr, GlslExpr>> INT_UNPACKING_FUNCS = new EnumMap<>(IntegerRepr.class);
	private static final EnumMap<UnsignedIntegerRepr, Function<GlslExpr, GlslExpr>> UINT_UNPACKING_FUNCS = new EnumMap<>(UnsignedIntegerRepr.class);
	private static final EnumMap<FloatRepr, Function<GlslExpr, GlslExpr>> FLOAT_UNPACKING_FUNCS = new EnumMap<>(FloatRepr.class);

	static {
		INT_UNPACKING_FUNCS.put(IntegerRepr.BYTE, e -> signExtendByte(e).cast("int"));
		INT_UNPACKING_FUNCS.put(IntegerRepr.SHORT, e -> signExtendShort(e).cast("int"));
		INT_UNPACKING_FUNCS.put(IntegerRepr.INT, e -> e.cast("int"));

		UINT_UNPACKING_FUNCS.put(UnsignedIntegerRepr.UNSIGNED_BYTE, Function.identity());
		UINT_UNPACKING_FUNCS.put(UnsignedIntegerRepr.UNSIGNED_SHORT, Function.identity());
		UINT_UNPACKING_FUNCS.put(UnsignedIntegerRepr.UNSIGNED_INT, Function.identity());

		FLOAT_UNPACKING_FUNCS.put(FloatRepr.BYTE, e -> signExtendByte(e).cast("int").cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_BYTE, e -> signExtendByte(e).cast("int").cast("float").div(127f).clamp(-1, 1));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.UNSIGNED_BYTE, e -> e.cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_UNSIGNED_BYTE, e -> e.cast("float").div(255f));

		FLOAT_UNPACKING_FUNCS.put(FloatRepr.SHORT, e -> signExtendShort(e).cast("int").cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_SHORT, e -> signExtendShort(e).cast("int").cast("float").div(32767f).clamp(-1, 1));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.UNSIGNED_SHORT, e -> e.cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_UNSIGNED_SHORT, e -> e.cast("float").div(65535f));

		FLOAT_UNPACKING_FUNCS.put(FloatRepr.INT, e -> e.cast("int").cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_INT, e -> e.cast("int").cast("float").div(2147483647f).clamp(-1, 1));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.UNSIGNED_INT, e -> e.cast("float"));
		FLOAT_UNPACKING_FUNCS.put(FloatRepr.NORMALIZED_UNSIGNED_INT, e -> e.cast("float").div(4294967295f));

		FLOAT_UNPACKING_FUNCS.put(FloatRepr.FLOAT, e -> e.callFunction("uintBitsToFloat")); // FIXME: GLSL 330+
	}

	public BufferTextureInstanceComponent(InstanceType<?> type) {
		super(type);
	}

	public static BufferTextureInstanceComponent create(InstanceType<?> type) {
		return new BufferTextureInstanceComponent(type);
	}

	public static BufferTextureInstanceComponent create(Pipeline.InstanceAssemblerContext ctx) {
		return create(ctx.instanceType());
	}

	// https://graphics.stanford.edu/~seander/bithacks.html#FixedSignExtend
	// Assumes bits higher than sign bit are zero
	private static GlslExpr signExtendByte(GlslExpr e) {
		return e.xor(0x80).sub(0x80);
	}

	private static GlslExpr signExtendShort(GlslExpr e) {
		return e.xor(0x8000).sub(0x8000);
	}

	@Override
	public String name() {
		return Flywheel.rl("buffer_texture_instance_assembler").toString();
	}

	@Override
	protected void generateUnpacking(GlslBuilder builder) {
		var block = new GlslBlock();

		// TODO: don't require writing to be 16 byte aligned
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
			// Element byte size is always a multiple of 4
			uintOffset += element.type().byteSize() / 4;
		}

		block.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

		builder._addRaw("uniform usamplerBuffer _flw_instances;");
		builder.blankLine();
		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg("int", UNPACK_ARG)
						.build())
				.body(block);
	}

	private static GlslExpr unpackElement(Layout.Element element, int uintOffset) {
		var type = element.type();

		if (type instanceof ScalarElementType scalar) {
			return unpackScalar(scalar, uintOffset);
		} else if (type instanceof VectorElementType vector) {
			return unpackVector(vector, uintOffset);
		} else if (type instanceof MatrixElementType matrix) {
			return unpackMatrix(matrix, uintOffset);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	private static GlslExpr unpackScalar(ScalarElementType type, int uintOffset) {
		var repr = type.repr();
		Function<GlslExpr, GlslExpr> unpackingFunc;

		if (repr instanceof IntegerRepr intRepr) {
			unpackingFunc = INT_UNPACKING_FUNCS.get(intRepr);
		} else if (repr instanceof UnsignedIntegerRepr uintRepr) {
			unpackingFunc = UINT_UNPACKING_FUNCS.get(uintRepr);
		} else if (repr instanceof FloatRepr floatRepr) {
			unpackingFunc = FLOAT_UNPACKING_FUNCS.get(floatRepr);
		} else {
			throw new IllegalArgumentException("Unknown repr " + repr);
		}

		if (isByteBacked(repr)) {
			return unpackByteBackedScalar(uintOffset, unpackingFunc);
		} else if (isShortBacked(repr)) {
			return unpackShortBackedScalar(uintOffset, unpackingFunc);
		} else {
			return unpackIntBackedScalar(uintOffset, unpackingFunc);
		}
	}

	private static GlslExpr unpackVector(VectorElementType type, int uintOffset) {
		var repr = type.repr();
		int size = type.size();
		Function<GlslExpr, GlslExpr> unpackingFunc;
		String outType;

		if (repr instanceof IntegerRepr intRepr) {
			unpackingFunc = INT_UNPACKING_FUNCS.get(intRepr);
			outType = "ivec" + size;
		} else if (repr instanceof UnsignedIntegerRepr uintRepr) {
			unpackingFunc = UINT_UNPACKING_FUNCS.get(uintRepr);
			outType = "uvec" + size;
		} else if (repr instanceof FloatRepr floatRepr) {
			unpackingFunc = FLOAT_UNPACKING_FUNCS.get(floatRepr);
			outType = "vec" + size;
		} else {
			throw new IllegalArgumentException("Unknown repr " + repr);
		}

		if (isByteBacked(repr)) {
			return unpackByteBackedVector(outType, size, uintOffset, unpackingFunc);
		} else if (isShortBacked(repr)) {
			return unpackShortBackedVector(outType, size, uintOffset, unpackingFunc);
		} else {
			return unpackIntBackedVector(outType, size, uintOffset, unpackingFunc);
		}
	}

	private static GlslExpr unpackMatrix(MatrixElementType type, int uintOffset) {
		var repr = type.repr();
		int rows = type.rows();
		int columns = type.columns();
		Function<GlslExpr, GlslExpr> unpackingFunc = FLOAT_UNPACKING_FUNCS.get(repr);
		String outType = "mat" + columns + "x" + rows;
		int size = rows * columns;

		if (isByteBacked(repr)) {
			return unpackByteBackedVector(outType, size, uintOffset, unpackingFunc);
		} else if (isShortBacked(repr)) {
			return unpackShortBackedVector(outType, size, uintOffset, unpackingFunc);
		} else {
			return unpackIntBackedVector(outType, size, uintOffset, unpackingFunc);
		}
	}

	private static boolean isByteBacked(ValueRepr repr) {
		return repr.byteSize() == Byte.BYTES;
	}

	private static boolean isShortBacked(ValueRepr repr) {
		return repr.byteSize() == Short.BYTES;
	}

	private static GlslExpr unpackByteBackedScalar(int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		GlslExpr e;
		if (BIG_ENDIAN) {
			e = access(uintOffset)
					.rsh(24)
					.and(0xFF);
		} else {
			e = access(uintOffset)
					.and(0xFF);
		}
		return perElement.apply(e);
	}

	private static GlslExpr unpackShortBackedScalar(int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		GlslExpr e;
		if (BIG_ENDIAN) {
			e = access(uintOffset)
					.rsh(16)
					.and(0xFFFF);
		} else {
			e = access(uintOffset)
					.and(0xFFFF);
		}
		return perElement.apply(e);
	}

	private static GlslExpr unpackIntBackedScalar(int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		return perElement.apply(access(uintOffset));
	}

	private static GlslExpr unpackByteBackedVector(String outType, int size, int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			// Vectors cannot contain more than 4 elements, but matrix unpacking treats the matrix as a long vector, which for mat4x4 would be the equivalent of a vec16.
			int bitPos = (i % 4) * 8;
			if (BIG_ENDIAN) {
				bitPos = 24 - bitPos;
			}
			int wordOffset = i / 4;
			var element = access(uintOffset + wordOffset)
					.rsh(bitPos)
					.and(0xFF);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackShortBackedVector(String outType, int size, int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int bitPos = (i % 2) * 16;
			if (BIG_ENDIAN) {
				bitPos = 16 - bitPos;
			}
			int wordOffset = i / 2;
			var element = access(uintOffset + wordOffset)
					.rsh(bitPos)
					.and(0xFFFF);
			args.add(perElement.apply(element));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr unpackIntBackedVector(String outType, int size, int uintOffset, Function<GlslExpr, GlslExpr> perElement) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			args.add(perElement.apply(access(uintOffset + i)));
		}
		return GlslExpr.call(outType, args);
	}

	private static GlslExpr access(int uintOffset) {
		return GlslExpr.variable("u" + (uintOffset >> 2))
				.swizzle(SWIZZLE_SELECTORS[uintOffset & 3]);
	}
}
