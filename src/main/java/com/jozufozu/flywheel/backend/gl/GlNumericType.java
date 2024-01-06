package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL11;

public enum GlNumericType {
	FLOAT(4, "float", GL11.GL_FLOAT),
	UBYTE(1, "ubyte", GL11.GL_UNSIGNED_BYTE),
	BYTE(1, "byte", GL11.GL_BYTE),
	USHORT(2, "ushort", GL11.GL_UNSIGNED_SHORT),
	SHORT(2, "short", GL11.GL_SHORT),
	UINT(4, "uint", GL11.GL_UNSIGNED_INT),
	INT(4, "int", GL11.GL_INT),
	DOUBLE(8, "double", GL11.GL_DOUBLE),
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
