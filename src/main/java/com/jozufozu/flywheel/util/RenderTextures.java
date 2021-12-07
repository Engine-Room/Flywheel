package com.jozufozu.flywheel.util;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

/**
 * Helper class to keep track of what texture atlases are bound to what texture units.
 *
 * <p>
 *     Works with {@link com.jozufozu.flywheel.mixin.RenderTexturesMixin}.
 * </p>
 */
public class RenderTextures {

	private static final ResourceLocation[] shaderTextures = new ResourceLocation[12];

	@Nullable
	public static ResourceLocation getShaderTexture(int pShaderTexture) {
		return shaderTextures[pShaderTexture];
	}

	public static void _setShaderTexture(int pShaderTexture, ResourceLocation pTextureId) {
		shaderTextures[pShaderTexture] = pTextureId;
	}
}
