package com.jozufozu.flywheel.backend.compile.component;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.ArrayElementType;
import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.backend.compile.LayoutInterpreter;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;

public abstract class InstanceAssemblerComponent implements SourceComponent {
	protected static final String STRUCT_NAME = "FlwInstance";
	protected static final String UNPACK_FN_NAME = "_flw_unpackInstance";
	protected static final String UNPACK_ARG = "index";

	private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

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

	protected final Layout layout;

	public InstanceAssemblerComponent(InstanceType<?> type) {
		layout = type.layout();
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
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();
		generateUnpacking(builder);
		builder.blankLine();
		return builder.build();
	}

	protected abstract void generateUnpacking(GlslBuilder builder);

	protected abstract GlslExpr access(int uintOffset);

	protected GlslExpr unpackElement(Layout.Element element) {
		return unpackElement(element.type(), element.byteOffset());
	}

	private GlslExpr unpackElement(ElementType type, int byteOffset) {
		if (type instanceof ScalarElementType scalar) {
			return unpackScalar(scalar, byteOffset);
		} else if (type instanceof VectorElementType vector) {
			return unpackVector(vector, byteOffset);
		} else if (type instanceof MatrixElementType matrix) {
			return unpackMatrix(matrix, byteOffset);
		} else if (type instanceof ArrayElementType array) {
			return unpackArray(array, byteOffset);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	private GlslExpr unpackScalar(ScalarElementType type, int byteOffset) {
		var repr = type.repr();
		Function<GlslExpr, GlslExpr> unpackingFunc = getUnpackingFunc(repr);
		return unpackScalar(byteOffset, repr.byteSize(), unpackingFunc);
	}

	private GlslExpr unpackVector(VectorElementType type, int byteOffset) {
		var repr = type.repr();
		int size = type.size();
		Function<GlslExpr, GlslExpr> unpackingFunc = getUnpackingFunc(repr);
		String outType = LayoutInterpreter.vectorTypeName(type);
		return unpackVector(outType, size, byteOffset, repr.byteSize(), unpackingFunc);
	}

	private GlslExpr unpackMatrix(MatrixElementType type, int byteOffset) {
		var repr = type.repr();
		int rows = type.rows();
		int columns = type.columns();
		Function<GlslExpr, GlslExpr> unpackingFunc = FLOAT_UNPACKING_FUNCS.get(repr);
		String outType = LayoutInterpreter.matrixTypeName(type);
		int size = rows * columns;
		return unpackVector(outType, size, byteOffset, repr.byteSize(), unpackingFunc);
	}

	private GlslExpr unpackArray(ArrayElementType type, int byteOffset) {
		ElementType innerType = type.innerType();
		int innerByteSize = innerType.byteSize();
		int length = type.length();
		String outType = LayoutInterpreter.arrayTypeName(type);

		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			args.add(unpackElement(innerType, byteOffset + i * innerByteSize));
		}
		return GlslExpr.call(outType, args);
	}

	private GlslExpr unpackScalar(int byteOffset, int byteSize, Function<GlslExpr, GlslExpr> unpackingFunc) {
		int offset = byteOffset / byteSize;

		if (byteSize == Byte.BYTES) {
			return unpackByteBackedScalar(offset, unpackingFunc);
		} else if (byteSize == Short.BYTES) {
			return unpackShortBackedScalar(offset, unpackingFunc);
		} else {
			return unpackIntBackedScalar(offset, unpackingFunc);
		}
	}

	private GlslExpr unpackByteBackedScalar(int byteOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		int bitPos = (byteOffset % 4) * 8;
		if (BIG_ENDIAN) {
			bitPos = 24 - bitPos;
		}
		int wordOffset = byteOffset / 4;
		GlslExpr prepared = access(wordOffset)
				.rsh(bitPos)
				.and(0xFF);
		return unpackingFunc.apply(prepared);
	}

	private GlslExpr unpackShortBackedScalar(int shortOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		int bitPos = (shortOffset % 2) * 16;
		if (BIG_ENDIAN) {
			bitPos = 16 - bitPos;
		}
		int wordOffset = shortOffset / 2;
		GlslExpr prepared = access(wordOffset)
				.rsh(bitPos)
				.and(0xFFFF);
		return unpackingFunc.apply(prepared);
	}

	private GlslExpr unpackIntBackedScalar(int intOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		return unpackingFunc.apply(access(intOffset));
	}

	private GlslExpr unpackVector(String outType, int size, int byteOffset, int byteSize, Function<GlslExpr, GlslExpr> unpackingFunc) {
		int offset = byteOffset / byteSize;

		if (byteSize == Byte.BYTES) {
			return unpackByteBackedVector(outType, size, offset, unpackingFunc);
		} else if (byteSize == Short.BYTES) {
			return unpackShortBackedVector(outType, size, offset, unpackingFunc);
		} else {
			return unpackIntBackedVector(outType, size, offset, unpackingFunc);
		}
	}

	private GlslExpr unpackByteBackedVector(String outType, int size, int byteOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			args.add(unpackByteBackedScalar(byteOffset + i, unpackingFunc));
		}
		return GlslExpr.call(outType, args);
	}

	private GlslExpr unpackShortBackedVector(String outType, int size, int shortOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			args.add(unpackShortBackedScalar(shortOffset + i, unpackingFunc));
		}
		return GlslExpr.call(outType, args);
	}

	private GlslExpr unpackIntBackedVector(String outType, int size, int intOffset, Function<GlslExpr, GlslExpr> unpackingFunc) {
		List<GlslExpr> args = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			args.add(unpackIntBackedScalar(intOffset + i, unpackingFunc));
		}
		return GlslExpr.call(outType, args);
	}

	private static Function<GlslExpr, GlslExpr> getUnpackingFunc(ValueRepr repr) {
		if (repr instanceof IntegerRepr intRepr) {
			return INT_UNPACKING_FUNCS.get(intRepr);
		} else if (repr instanceof UnsignedIntegerRepr uintRepr) {
			return UINT_UNPACKING_FUNCS.get(uintRepr);
		} else if (repr instanceof FloatRepr floatRepr) {
			return FLOAT_UNPACKING_FUNCS.get(floatRepr);
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}
}
