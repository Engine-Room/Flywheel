package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.opengl.GlConst;

public enum GlPrimitive {
	POINTS(GlConst.GL_POINTS),
	LINES(GlConst.GL_LINES),
	LINE_LOOP(GL11.GL_LINE_LOOP),
	LINE_STRIP(GlConst.GL_LINE_STRIP),
	TRIANGLES(GlConst.GL_TRIANGLES),
	TRIANGLE_STRIP(GlConst.GL_TRIANGLE_STRIP),
	TRIANGLE_FAN(GlConst.GL_TRIANGLE_FAN),
	QUADS(GL11.GL_QUADS),
	QUAD_STRIP(GL11.GL_QUAD_STRIP),
	POLYGON(GL11.GL_POLYGON),
	;

	public final int glEnum;

	GlPrimitive(int glEnum) {
		this.glEnum = glEnum;
	}
}
