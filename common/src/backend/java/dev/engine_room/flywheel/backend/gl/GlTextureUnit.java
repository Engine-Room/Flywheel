package dev.engine_room.flywheel.backend.gl;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.opengl.GlStateManager;

public enum GlTextureUnit {
	T0(0),
	T1(1),
	T2(2),
	T3(3),
	T4(4),
	T5(5),
	T6(6),
	T7(7),
	T8(8),
	T9(9),
	T10(10),
	T11(11),
	T12(12),
	T13(13),
	T14(14),
	T15(15),
	T16(16),
	T17(17),
	T18(18),
	T19(19),
	T20(20),
	T21(21),
	T22(22),
	T23(23),
	T24(24),
	T25(25),
	T26(26),
	T27(27),
	T28(28),
	T29(29),
	T30(30),
	T31(31),

	;

	public final int number;
	public final int glEnum;

	GlTextureUnit(int unit) {
		this.number = unit;
		this.glEnum = GL_TEXTURE0 + unit;
	}

	public void makeActive() {
		GlStateManager._activeTexture(glEnum);
	}

	public static GlTextureUnit getActive() {
		return fromGlEnum(GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE));
	}

	public static GlTextureUnit fromGlEnum(int glEnum) {
		return GlTextureUnit.values()[glEnum - GL_TEXTURE0];
	}
}
