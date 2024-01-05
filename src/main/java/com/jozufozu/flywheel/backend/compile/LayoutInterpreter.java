package com.jozufozu.flywheel.backend.compile;

import com.jozufozu.flywheel.api.layout.ElementType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.MatrixElementType;
import com.jozufozu.flywheel.api.layout.ScalarElementType;
import com.jozufozu.flywheel.api.layout.UnsignedIntegerRepr;
import com.jozufozu.flywheel.api.layout.ValueRepr;
import com.jozufozu.flywheel.api.layout.VectorElementType;

public class LayoutInterpreter {
	public static int attributeCount(ElementType type) {
		if (type instanceof ScalarElementType) {
			return 1;
		} else if (type instanceof VectorElementType) {
			return 1;
		} else if (type instanceof MatrixElementType matrix) {
			return matrix.rows();
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	public static String typeName(ElementType type) {
		if (type instanceof ScalarElementType scalar) {
			return scalarTypeName(scalar.repr());
		} else if (type instanceof VectorElementType vector) {
			return vectorTypeName(vector.repr(), vector.size());
		} else if (type instanceof MatrixElementType matrix) {
			return matrixTypeName(matrix);
		}

		throw new IllegalArgumentException("Unknown type " + type);
	}

	public static String matrixTypeName(MatrixElementType matrix) {
		return "mat" + matrix.columns() + "x" + matrix.rows();
	}

	public static String vectorTypeName(ValueRepr repr, int size) {
		if (repr instanceof IntegerRepr) {
			return "ivec" + size;
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uvec" + size;
		} else if (repr instanceof FloatRepr) {
			return "vec" + size;
		}
		throw new IllegalArgumentException("Unknown repr " + repr);
	}

	public static String scalarTypeName(ValueRepr repr) {
		if (repr instanceof IntegerRepr) {
			return "int";
		} else if (repr instanceof UnsignedIntegerRepr) {
			return "uint";
		} else if (repr instanceof FloatRepr) {
			return "float";
		}
		throw new IllegalArgumentException("Unknown repr " + repr);
	}
}
