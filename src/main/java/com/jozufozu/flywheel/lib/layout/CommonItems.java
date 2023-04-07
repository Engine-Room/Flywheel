package com.jozufozu.flywheel.lib.layout;

import com.jozufozu.flywheel.gl.GlNumericType;
import com.jozufozu.flywheel.gl.array.VertexAttributeF;
import com.jozufozu.flywheel.gl.array.VertexAttributeI;

public class CommonItems {
	private static final String VEC2_TYPE = "vec2";
	private static final String VEC3_TYPE = "vec3";
	private static final String VEC4_TYPE = "vec4";
	private static final String VEC2F_TYPE = "Vec2F";
	private static final String VEC3F_TYPE = "Vec3F";
	private static final String VEC4F_TYPE = "Vec4F";
	private static final String IVEC2_TYPE = "ivec2";
	private static final String FLOAT_TYPE = "float";
	private static final String UINT_TYPE = "uint";
	private static final String LIGHT_COORD_TYPE = "LightCoord";

	public static final VecInput LIGHT_COORD = VecInput.builder()
			.vertexAttribute(new VertexAttributeI(GlNumericType.USHORT, 2))
			.typeName(IVEC2_TYPE)
			.packedTypeName(LIGHT_COORD_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackLightCoord"))
			.build();

	public static final VecInput FLOAT = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.FLOAT, 1, false))
			.typeName(FLOAT_TYPE)
			.packedTypeName(FLOAT_TYPE)
			.build();
	public static final VecInput NORM_3x8 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.BYTE, 3, true))
			.typeName(VEC3_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackSnorm4x8")
					.swizzle("xyz"))
			.build();
	public static final VecInput UNORM_4x8 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.UBYTE, 4, true))
			.typeName(VEC4_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackUnorm4x8"))
			.build();
	public static final VecInput UNORM_3x8 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.UBYTE, 3, true))
			.typeName(VEC3_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackUnorm4x8")
					.swizzle("xyz"))
			.build();
	public static final VecInput VEC4 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.FLOAT, 4, false))
			.typeName(VEC4_TYPE)
			.packedTypeName(VEC4F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec4F"))
			.build();
	public static final VecInput VEC3 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.FLOAT, 3, false))
			.typeName(VEC3_TYPE)
			.packedTypeName(VEC3F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec3F"))
			.build();
	public static final VecInput VEC2 = VecInput.builder()
			.vertexAttribute(new VertexAttributeF(GlNumericType.FLOAT, 2, false))
			.typeName(VEC2_TYPE)
			.packedTypeName(VEC2F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec2F"))
			.build();

	public static final MatInput MAT3 = new MatInput(3, 3, "mat3", "Mat3F", "unpackMat3F");
	public static final MatInput MAT4 = new MatInput(4, 4, "mat4", "Mat4F", "unpackMat4F");
}
