package dev.engine_room.flywheel.backend;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.layout.ArrayElementType;
import dev.engine_room.flywheel.api.layout.ElementType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.IntegerRepr;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.api.layout.MatrixElementType;
import dev.engine_room.flywheel.api.layout.ScalarElementType;
import dev.engine_room.flywheel.api.layout.UnsignedIntegerRepr;
import dev.engine_room.flywheel.api.layout.ValueRepr;
import dev.engine_room.flywheel.api.layout.VectorElementType;
import dev.engine_room.flywheel.backend.gl.GlNumericType;
import dev.engine_room.flywheel.backend.gl.array.VertexAttribute;

public class LayoutAttributes {
	/**
	 * Collects the vertex attributes required from the given layout.
	 *
	 * @param layout The abstract layout definition.
	 * @return A concrete list of vertex attributes.
	 */
	public static List<VertexAttribute> attributes(Layout layout) {
		List<VertexAttribute> out = new ArrayList<>();

		for (Layout.Element element : layout.elements()) {
			element(out, element.type());
		}

		return out;
	}

	private static void element(List<VertexAttribute> out, ElementType type) {
		if (type instanceof ScalarElementType scalar) {
			vector(out, scalar.repr(), 1);
		} else if (type instanceof VectorElementType vector) {
			vector(out, vector.repr(), vector.size());
		} else if (type instanceof MatrixElementType matrix) {
			matrix(out, matrix);
		} else if (type instanceof ArrayElementType array) {
			array(out, array);
		} else {
			throw new IllegalArgumentException("Unknown type " + type);
		}
	}

	private static void vector(List<VertexAttribute> out, ValueRepr repr, int size) {
		if (repr instanceof IntegerRepr integer) {
			out.add(new VertexAttribute.Int(toGlType(integer), size));
		} else if (repr instanceof UnsignedIntegerRepr integer) {
			out.add(new VertexAttribute.Int(toGlType(integer), size));
		} else if (repr instanceof FloatRepr floatRepr) {
			out.add(new VertexAttribute.Float(toGlType(floatRepr), size, isNormalized(floatRepr)));
		} else {
			throw new IllegalArgumentException("Unknown repr " + repr);
		}
	}

	private static void matrix(List<VertexAttribute> out, MatrixElementType matrix) {
		int size = matrix.columns();
		var repr = matrix.repr();
		var glType = toGlType(repr);
		boolean normalized = isNormalized(repr);

		for (int i = 0; i < matrix.rows(); i++) {
			out.add(new VertexAttribute.Float(glType, size, normalized));
		}
	}

	private static void array(List<VertexAttribute> out, ArrayElementType array) {
		ElementType innerType = array.innerType();
		int length = array.length();

		for (int i = 0; i < length; i++) {
			element(out, innerType);
		}
	}

	private static GlNumericType toGlType(IntegerRepr repr) {
		return switch (repr) {
			case BYTE -> GlNumericType.BYTE;
			case SHORT -> GlNumericType.SHORT;
			case INT -> GlNumericType.INT;
		};
	}

	private static GlNumericType toGlType(UnsignedIntegerRepr repr) {
		return switch (repr) {
			case UNSIGNED_BYTE -> GlNumericType.UBYTE;
			case UNSIGNED_SHORT -> GlNumericType.USHORT;
			case UNSIGNED_INT -> GlNumericType.UINT;
		};
	}

	private static GlNumericType toGlType(FloatRepr repr) {
		return switch (repr) {
			case BYTE, NORMALIZED_BYTE -> GlNumericType.BYTE;
			case UNSIGNED_BYTE, NORMALIZED_UNSIGNED_BYTE -> GlNumericType.UBYTE;
			case SHORT, NORMALIZED_SHORT -> GlNumericType.SHORT;
			case UNSIGNED_SHORT, NORMALIZED_UNSIGNED_SHORT -> GlNumericType.USHORT;
			case INT, NORMALIZED_INT -> GlNumericType.INT;
			case UNSIGNED_INT, NORMALIZED_UNSIGNED_INT -> GlNumericType.UINT;
			case FLOAT -> GlNumericType.FLOAT;
		};
	}

	private static boolean isNormalized(FloatRepr repr) {
		return switch (repr) {
			case NORMALIZED_BYTE, NORMALIZED_UNSIGNED_BYTE, NORMALIZED_SHORT, NORMALIZED_UNSIGNED_SHORT, NORMALIZED_INT, NORMALIZED_UNSIGNED_INT ->
					true;
			default -> false;
		};
	}
}
