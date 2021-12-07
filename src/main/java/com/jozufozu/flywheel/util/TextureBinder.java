package com.jozufozu.flywheel.util;

import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;

/**
 * This is a silly hack that's needed because flywheel does things too different from vanilla.
 *
 * <p>
 *     When a {@link RenderType} is setup, the associated textures are "bound" within RenderSystem, but not actually
 *     bound via opengl. This class provides a helper function to forward the bindings to opengl.
 * </p>
 */
public class TextureBinder {

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
