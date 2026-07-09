package dev.engine_room.flywheel.backend.engine;

import org.lwjgl.opengl.GL11;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.gl.GlTextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class TextureBinder {
	public static void bind(Identifier Identifier) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, byName(Identifier));
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		Samplers.OVERLAY.makeActive();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlTextureUtil.glId(gameRenderer.overlayTexture()
				.getTextureView()));

		Samplers.LIGHT.makeActive();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GlTextureUtil.glId(gameRenderer.lightmap()));
	}

	public static void resetLightAndOverlay() {
	}

	/**
	 * Get a built-in texture by its resource location.
	 *
	 * @param texture The texture's resource location.
	 * @return The texture.
	 */
	public static int byName(Identifier texture) {
		var abstractTexture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(texture);
		var textureView = abstractTexture.getTextureView();
		return textureView == null ? 0 : GlTextureUtil.glId(textureView);
	}
}
