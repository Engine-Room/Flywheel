package com.jozufozu.flywheel.core.layout;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeF;
import com.jozufozu.flywheel.backend.gl.array.VertexAttributeI;

public class CommonItems {

	private static final String VEC3_TYPE = "vec3";
	private static final String VEC4_TYPE = "vec4";
	private static final String VEC2_TYPE = "vec2";
	private static final String FLOAT_TYPE = "float";
	public static final PrimitiveItem FLOAT = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.FLOAT, 1, false))
			.setTypeName(FLOAT_TYPE)
			.setPackedTypeName(FLOAT_TYPE)
			.createPrimitiveItem();
	private static final String UINT_TYPE = "uint";
	public static final PrimitiveItem NORM_3x8 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.BYTE, 3, true))
			.setTypeName(VEC3_TYPE)
			.setPackedTypeName(UINT_TYPE)
			.unpack(expr -> expr.callFunction("unpackSnorm4x8")
					.swizzle("xyz"))
			.createPrimitiveItem();
	public static final PrimitiveItem UNORM_4x8 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.UBYTE, 4, true))
			.setTypeName(VEC4_TYPE)
			.setPackedTypeName(UINT_TYPE)
			.unpack(expr -> expr.callFunction("unpackUnorm4x8"))
			.createPrimitiveItem();
	public static final PrimitiveItem UNORM_3x8 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.UBYTE, 3, true))
			.setTypeName(VEC3_TYPE)
			.setPackedTypeName(UINT_TYPE)
			.unpack(expr -> expr.callFunction("unpackUnorm4x8")
					.swizzle("xyz"))
			.createPrimitiveItem();
	private static final String IVEC2_TYPE = "ivec2";
	private static final String VEC4F_TYPE = "Vec4F";
	public static final PrimitiveItem VEC4 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.FLOAT, 4, false))
			.setTypeName(VEC4_TYPE)
			.setPackedTypeName(VEC4F_TYPE)
			.unpack(expr -> expr.callFunction("unpackVec4F"))
			.createPrimitiveItem();
	private static final String VEC3F_TYPE = "Vec3F";
	public static final PrimitiveItem VEC3 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.FLOAT, 3, false))
			.setTypeName(VEC3_TYPE)
			.setPackedTypeName(VEC3F_TYPE)
			.unpack(expr -> expr.callFunction("unpackVec3F"))
			.createPrimitiveItem();
	private static final String VEC2F_TYPE = "Vec2F";
	public static final PrimitiveItem VEC2 = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeF(GlNumericType.FLOAT, 2, false))
			.setTypeName(VEC2_TYPE)
			.setPackedTypeName(VEC2F_TYPE)
			.unpack(expr -> expr.callFunction("unpackVec2F"))
			.createPrimitiveItem();
	private static final String LIGHT_COORD_TYPE = "LightCoord";
	public static final PrimitiveItem LIGHT_COORD = PrimitiveItem.builder()
			.setAttribute(new VertexAttributeI(GlNumericType.USHORT, 2))
			.setTypeName(VEC2_TYPE)
			.setPackedTypeName(LIGHT_COORD_TYPE)
			.unpack(expr -> expr.callFunction("unpackLightCoord"))
			.createPrimitiveItem();


	public static final MatrixItem MAT3 = new MatrixItem(3, 3, "mat3", "Mat3F", "unpackMat3F");
	public static final MatrixItem MAT4 = new MatrixItem(4, 4, "mat4", "Mat4F", "unpackMat4F");

	private static class Unpacking {

	}
}
