package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.layout.ArrayElementType;
import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;

public class LayoutInterpreter {
	public static String typeName(ElementType type) {
		if (type instanceof ScalarElementType scalar) {
			return scalarTypeName(scalar);
		} else if (type instanceof VectorElementType vector) {
			return vectorTypeName(vector);
		} else if (type instanceof MatrixElementType matrix) {
			return matrixTypeName(matrix);
		} else if (type instanceof ArrayElementType array) {
			return arrayTypeName(array);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	public static String scalarTypeName(ScalarElementType scalar) {
		ValueRepr repr = scalar.repr();

		if (repr instanceof IntegerRepr) {
			return "int";
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uint";
		} else if (repr instanceof FloatRepr) {
			return "float";
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	public static String vectorTypeName(VectorElementType vector) {
		ValueRepr repr = vector.repr();
		int size = vector.size();

		if (repr instanceof IntegerRepr) {
			return "ivec" + size;
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uvec" + size;
		} else if (repr instanceof FloatRepr) {
			return "vec" + size;
		}

		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	public static String matrixTypeName(MatrixElementType matrix) {
		return "mat" + matrix.columns() + "x" + matrix.rows();
	}

	// This will not produce correct types for multidimensional arrays (the lengths will be in the opposite order),
	// but the API does not allow creating multidimensional arrays anyway because they are not core until GLSL 430.
	public static String arrayTypeName(ArrayElementType array) {
		return typeName(array.innerType()) + "[" + array.length() + "]";
	}
}
