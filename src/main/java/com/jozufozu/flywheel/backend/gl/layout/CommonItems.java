package com.jozufozu.flywheel.backend.gl.layout;

import com.jozufozu.flywheel.backend.gl.GlNumericType;
import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.backend.glsl.generate.FnSignature;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;

public final class CommonItems {
	private static final String VEC2_TYPE = "vec2";
	private static final String VEC3_TYPE = "vec3";
	private static final String VEC4_TYPE = "vec4";
	private static final String VEC2F_TYPE = "Vec2F";
	private static final String VEC3F_TYPE = "Vec3F";
	private static final String VEC4F_TYPE = "Vec4F";
	private static final String IVEC2_TYPE = "ivec2";
	private static final String FLOAT_TYPE = "float";
	private static final String UINT_TYPE = "uint";
	private static final String LIGHT_COORD_TYPE = "uint";

	public static final VecInput LIGHT_COORD = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Int(GlNumericType.USHORT, 2))
			.typeName(IVEC2_TYPE)
			.packedTypeName(LIGHT_COORD_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackLightCoord"))
			.declaration(builder -> builder.function()
					.signature(FnSignature.create()
							.name("unpackLightCoord")
							.returnType(IVEC2_TYPE)
							.arg("uint", "light")
							.build())
					.body(block -> block.raw("return ivec2(light & 0xFFFFu, (light >> 16) & 0xFFFFu);")))
			.build();

	public static final VecInput FLOAT = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.FLOAT, 1, false))
			.typeName(FLOAT_TYPE)
			.packedTypeName(FLOAT_TYPE)
			.build();
	public static final VecInput NORM_3x8 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.BYTE, 3, true))
			.typeName(VEC3_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackSnorm4x8")
					.swizzle("xyz"))
			.build();
	public static final VecInput UNORM_4x8 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.UBYTE, 4, true))
			.typeName(VEC4_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackUnorm4x8"))
			.build();
	public static final VecInput UNORM_3x8 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.UBYTE, 3, true))
			.typeName(VEC3_TYPE)
			.packedTypeName(UINT_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackUnorm4x8")
					.swizzle("xyz"))
			.build();
	public static final VecInput VEC4 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.FLOAT, 4, false))
			.typeName(VEC4_TYPE)
			.packedTypeName(VEC4F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec4F"))
			.declaration(builder -> {
				builder.struct()
						.setName(VEC4F_TYPE)
						.addField(FLOAT_TYPE, "x")
						.addField(FLOAT_TYPE, "y")
						.addField(FLOAT_TYPE, "z")
						.addField(FLOAT_TYPE, "w");
				builder.function()
						.signature(FnSignature.create()
								.name("unpackVec4F")
								.returnType(VEC4_TYPE)
								.arg(VEC4F_TYPE, "v")
								.build())
						.body(block -> {
							var v = GlslExpr.variable("v");
							block.ret(GlslExpr.call("vec4", v.access("x"), v.access("y"), v.access("z"), v.access("w")));
						});
			})
			.build();
	public static final VecInput VEC3 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.FLOAT, 3, false))
			.typeName(VEC3_TYPE)
			.packedTypeName(VEC3F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec3F"))
			.declaration(builder -> {
				builder.struct()
						.setName(VEC3F_TYPE)
						.addField(FLOAT_TYPE, "x")
						.addField(FLOAT_TYPE, "y")
						.addField(FLOAT_TYPE, "z");
				builder.function()
						.signature(FnSignature.create()
								.name("unpackVec3F")
								.returnType(VEC3_TYPE)
								.arg(VEC3F_TYPE, "v")
								.build())
						.body(block -> {
							var v = GlslExpr.variable("v");
							block.ret(GlslExpr.call("vec3", v.access("x"), v.access("y"), v.access("z")));
						});
			})
			.build();
	public static final VecInput VEC2 = VecInput.builder()
			.vertexAttribute(new VertexAttribute.Float(GlNumericType.FLOAT, 2, false))
			.typeName(VEC2_TYPE)
			.packedTypeName(VEC2F_TYPE)
			.unpackingFunction(expr -> expr.callFunction("unpackVec2F"))
			.declaration(builder -> {
				builder.struct()
						.setName(VEC2F_TYPE)
						.addField(FLOAT_TYPE, "x")
						.addField(FLOAT_TYPE, "y");
				builder.function()
						.signature(FnSignature.create()
								.name("unpackVec2F")
								.returnType(VEC2_TYPE)
								.arg(VEC2F_TYPE, "v")
								.build())
						.body(block -> {
							var v = GlslExpr.variable("v");
							block.ret(GlslExpr.call("vec2", v.access("x"), v.access("y")));
						});
			})
			.build();

	public static final MatInput MAT3 = new MatInput(3, 3, "mat3", "Mat3F", "unpackMat3F");
	public static final MatInput MAT4 = new MatInput(4, 4, "mat4", "Mat4F", "unpackMat4F");

	private CommonItems() {
	}
}
