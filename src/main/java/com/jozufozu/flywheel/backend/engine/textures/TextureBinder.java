package com.jozufozu.flywheel.backend.engine.textures;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.client.Minecraft;

public class TextureBinder {
	// TODO: some kind of cache eviction when the program changes
	//  so we don't always reset and bind the light and overlay textures?
	private static final Int2IntMap texturesToSamplerUnits = new Int2IntArrayMap();
	// 0 is reserved for diffuse
	// 1 is overlay
	// 2 is light
	// 3..n are for whatever else the context needs
	private static final int baseSamplerUnit = 3;
	private static int nextSamplerUnit = baseSamplerUnit;

	/**
	 * Binds the given texture to the next available texture unit, returning the unit it was bound to.
	 *
	 * @param id The id of the texture to bind.
	 * @return The texture unit the texture was bound to.
	 */
	public static int bindTexture(int id) {
		return texturesToSamplerUnits.computeIfAbsent(id, i -> {
			int unit = nextSamplerUnit++;
			RenderSystem.activeTexture(GL_TEXTURE0 + unit);
			RenderSystem.bindTexture(i);
			return unit;
		});
	}

	public static void resetTextureBindings() {
		nextSamplerUnit = baseSamplerUnit;

		for (Int2IntMap.Entry entry : texturesToSamplerUnits.int2IntEntrySet()) {
			RenderSystem.activeTexture(GL_TEXTURE0 + entry.getIntValue());
			RenderSystem.bindTexture(0);
		}

		texturesToSamplerUnits.clear();
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;
		gameRenderer.overlayTexture()
				.setupOverlayColor();
		gameRenderer.lightTexture()
				.turnOnLightLayer();

		GlTextureUnit.T1.makeActive();
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(1));
		GlTextureUnit.T2.makeActive();
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));
	}

	public static void resetLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		gameRenderer.overlayTexture()
				.teardownOverlayColor();
		gameRenderer.lightTexture()
				.turnOffLightLayer();
	}
}
