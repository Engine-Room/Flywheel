package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;
import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.array.VertexAttribute;

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
			var type = element.type();

			if (type instanceof ScalarElementType scalar) {
				vector(out, scalar.repr(), 1);
			} else if (type instanceof VectorElementType vector) {
				vector(out, vector.repr(), vector.size());
			} else if (type instanceof MatrixElementType matrix) {
				matrix(out, matrix);
			}
		}

		return out;
	}

	private static void matrix(List<VertexAttribute> out, MatrixElementType matrix) {
		int size = matrix.columns();
		var repr = matrix.repr();
		var glType = toGlType(repr);
		boolean normalized = normalized(repr);

		for (int i = 0; i < matrix.rows(); i++) {
			out.add(new VertexAttribute.Float(glType, size, normalized));
		}
	}

	private static void vector(List<VertexAttribute> out, ValueRepr repr, int size) {
		if (repr instanceof IntegerRepr integer) {
			out.add(new VertexAttribute.Int(toGlType(integer), size));
		} else if (repr instanceof UnsignedIntegerRepr integer) {
			out.add(new VertexAttribute.Int(toGlType(integer), size));
		} else if (repr instanceof FloatRepr floatRepr) {
			out.add(new VertexAttribute.Float(toGlType(floatRepr), size, normalized(floatRepr)));
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

	private static boolean normalized(FloatRepr repr) {
		return switch (repr) {
			case NORMALIZED_BYTE, NORMALIZED_UNSIGNED_BYTE, NORMALIZED_SHORT, NORMALIZED_UNSIGNED_SHORT, NORMALIZED_INT, NORMALIZED_UNSIGNED_INT ->
					true;
			default -> false;
		};
	}
}
