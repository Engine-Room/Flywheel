package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.backend.Samplers;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class TextureBinder {
	public static void bind(ResourceLocation resourceLocation) {
		RenderSystem.bindTexture(byName(resourceLocation));
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		Samplers.OVERLAY.makeActive();
		gameRenderer.overlayTexture()
				.setupOverlayColor();
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(1));

		Samplers.LIGHT.makeActive();
		gameRenderer.lightTexture()
				.turnOnLightLayer();
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));
	}

	public static void resetLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		gameRenderer.overlayTexture()
				.teardownOverlayColor();
		gameRenderer.lightTexture()
				.turnOffLightLayer();
	}

	/**
	 * Get a built-in texture by its resource location.
	 *
	 * @param texture The texture's resource location.
	 * @return The texture.
	 */
	public static int byName(ResourceLocation texture) {
		return Minecraft.getInstance()
				.getTextureManager()
				.getTexture(texture)
				.getId();
	}
}
