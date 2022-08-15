package com.jozufozu.flywheel.core.layout;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeF;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeI;

public class CommonItems {

	public static final PrimitiveItem VEC4 = primitiveF(GlNumericType.FLOAT, 4);
	public static final PrimitiveItem VEC3 = primitiveF(GlNumericType.FLOAT, 3);
	public static final PrimitiveItem VEC2 = primitiveF(GlNumericType.FLOAT, 2);
	public static final PrimitiveItem FLOAT = primitiveF(GlNumericType.FLOAT, 1);

	public static final PrimitiveItem QUATERNION = primitiveF(GlNumericType.FLOAT, 4);
	public static final PrimitiveItem NORMAL = primitiveF(GlNumericType.BYTE, 3, true);
	public static final PrimitiveItem UV = primitiveF(GlNumericType.FLOAT, 2);

	public static final PrimitiveItem RGBA = primitiveF(GlNumericType.UBYTE, 4, true);
	public static final PrimitiveItem RGB = primitiveF(GlNumericType.UBYTE, 3, true);
	public static final PrimitiveItem LIGHT = primitiveI(GlNumericType.UBYTE, 2);
	public static final PrimitiveItem LIGHT_SHORT = primitiveI(GlNumericType.USHORT, 2);

	public static final PrimitiveItem NORMALIZED_BYTE = primitiveF(GlNumericType.BYTE, 1, true);

	public static final MatrixItem MAT3 = new MatrixItem(3, 3);
	public static final MatrixItem MAT4 = new MatrixItem(4, 4);

	private static PrimitiveItem primitiveF(GlNumericType type, int count, boolean normalized) {
		return new PrimitiveItem(new VertexAttributeF(type, count, normalized));
	}

	private static PrimitiveItem primitiveF(GlNumericType type, int count) {
		return primitiveF(type, count, false);
	}

	private static PrimitiveItem primitiveI(GlNumericType type, int count) {
		return new PrimitiveItem(new VertexAttributeI(type, count));
	}
}
