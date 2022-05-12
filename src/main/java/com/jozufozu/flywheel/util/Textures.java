package com.jozufozu.flywheel.util;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper class to keep track of what texture atlases are bound to what texture units.
 *
 * <p>
 *     Works with {@link com.jozufozu.flywheel.mixin.RenderTexturesMixin}.
 * </p>
 */
public class Textures {

	private static final ResourceLocation[] shaderTextures = new ResourceLocation[12];

	@Nullable
	public static ResourceLocation getShaderTexture(int pShaderTexture) {
		return shaderTextures[pShaderTexture];
	}

	public static void _setShaderTexture(int pShaderTexture, ResourceLocation pTextureId) {
		shaderTextures[pShaderTexture] = pTextureId;
	}

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
