package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class TextureBinder {
	public static void bind(Identifier id) {
		RenderSystem.bindTexture(byName(id));
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
	 * Get a built-in texture by its resource id.
	 *
	 * @param texture The texture's resource id.
	 * @return The texture.
	 */
	public static int byName(Identifier texture) {
		return Minecraft.getInstance()
				.getTextureManager()
				.getTexture(texture)
				.getId();
	}
}
