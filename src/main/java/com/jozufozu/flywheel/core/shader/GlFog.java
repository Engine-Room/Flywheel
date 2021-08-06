package com.jozufozu.flywheel.core.shader;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.FogRenderer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

public class GlFog {
	public static float[] FOG_COLOR = new float[]{0, 0, 0, 0};

	public static boolean fogEnabled() {
		return RenderSystem.getShaderFogStart() != Float.MAX_VALUE;
	}

//	public static int getFogModeGlEnum() {
//		return GlStateManager.FOG.mode;
//	}

	public static float getFogDensity() {
		return RenderSystem.getShaderFogColor()[4];
	}

	public static float getFogEnd() {
		return RenderSystem.getShaderFogEnd();
	}

	public static float getFogStart() {
		return RenderSystem.getShaderFogStart();
	}

	public static WorldFog getFogMode() {
		return WorldFog.LINEAR;
//		if (!fogEnabled()) {
//			return WorldFog.NONE;
//		}
//
//		int mode = getFogModeGlEnum();
//
//		switch (mode) {
//		case GL11.GL_EXP2:
//		case GL11.GL_EXP:
//			return WorldFog.EXP2;
//		case GL11.GL_LINEAR:
//			return WorldFog.LINEAR;
//		default:
//			throw new UnsupportedOperationException("Unknown fog mode: " + mode);
//		}
	}
}
