package com.jozufozu.flywheel.util;

import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;

public class Textures {
	/**
	 * Call this after calling {@link RenderType#setupRenderState()}.
	 */
	public static void bindActiveTextures() {
		for (int i = 0; i < 12; i++) {
			int shaderTexture = RenderSystem.getShaderTexture(i);
			RenderSystem.activeTexture(GL32.GL_TEXTURE0 + i);
			RenderSystem.bindTexture(shaderTexture);
		}
	}
}
