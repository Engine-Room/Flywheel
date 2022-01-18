package com.jozufozu.flywheel.core.layout;

import com.jozufozu.flywheel.backend.gl.GlNumericType;

public class CommonItems {

	public static final PrimitiveItem VEC4 = new PrimitiveItem(GlNumericType.FLOAT, 4);
	public static final PrimitiveItem VEC3 = new PrimitiveItem(GlNumericType.FLOAT, 3);
	public static final PrimitiveItem VEC2 = new PrimitiveItem(GlNumericType.FLOAT, 2);
	public static final PrimitiveItem FLOAT = new PrimitiveItem(GlNumericType.FLOAT, 1);

	public static final PrimitiveItem QUATERNION = new PrimitiveItem(GlNumericType.FLOAT, 4);
	public static final PrimitiveItem NORMAL = new PrimitiveItem(GlNumericType.BYTE, 3, true);
	public static final PrimitiveItem UV = new PrimitiveItem(GlNumericType.FLOAT, 2);

	public static final PrimitiveItem RGBA = new PrimitiveItem(GlNumericType.UBYTE, 4, true);
	public static final PrimitiveItem RGB = new PrimitiveItem(GlNumericType.UBYTE, 3, true);
	public static final PrimitiveItem LIGHT = new PrimitiveItem(GlNumericType.UBYTE, 2, true);
	public static final PrimitiveItem LIGHT_SHORT = new PrimitiveItem(GlNumericType.USHORT, 2, true);

	public static final PrimitiveItem NORMALIZED_BYTE = new PrimitiveItem(GlNumericType.BYTE, 1, true);
	public static final LayoutItem PADDING_BYTE = new Padding(1);

}
