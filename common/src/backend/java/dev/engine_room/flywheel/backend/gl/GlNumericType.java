package dev.engine_room.flywheel.backend.gl;

import com.mojang.blaze3d.opengl.GlConst;

public enum GlNumericType {
	FLOAT(4, "float", GlConst.GL_FLOAT),
	UBYTE(1, "ubyte", GlConst.GL_UNSIGNED_BYTE),
	BYTE(1, "byte", GlConst.GL_BYTE),
	USHORT(2, "ushort", GlConst.GL_UNSIGNED_SHORT),
	SHORT(2, "short", GlConst.GL_SHORT),
	UINT(4, "uint", GlConst.GL_UNSIGNED_INT),
	INT(4, "int", GlConst.GL_INT),
	DOUBLE(8, "double", GlConst.GL_DOUBLE),
	;

	public final int byteWidth;
	public final String typeName;
	public final int glEnum;

	GlNumericType(int bytes, String name, int glEnum) {
		this.byteWidth = bytes;
		this.typeName = name;
		this.glEnum = glEnum;
	}

	public int byteWidth() {
		return byteWidth;
	}

	public String typeName() {
		return typeName;
	}

	public int glEnum() {
		return glEnum;
	}

	@Override
	public String toString() {
		return typeName;
	}
}
