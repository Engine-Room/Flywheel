package com.jozufozu.flywheel.backend.engine.textures;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.context.Texture;
import com.jozufozu.flywheel.backend.mixin.LightTextureAccessor;
import com.jozufozu.flywheel.backend.mixin.OverlayTextureAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public class TextureSource {

	private final DirectTexture lightTexture;
	private final DirectTexture overlayTexture;
	private final Map<ResourceLocation, WrappedTexture> wrappers = new HashMap<>();

	public TextureSource() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		this.lightTexture = new DirectTexture(((LightTextureAccessor) gameRenderer.lightTexture()).flywheel$texture()
				.getId());
		this.overlayTexture = new DirectTexture(((OverlayTextureAccessor) gameRenderer.overlayTexture()).flywheel$texture()
				.getId());
	}


	/**
	 * Get a built-in texture by its resource location.
	 *
	 * @param texture The texture's resource location.
	 * @return The texture.
	 */
	public Texture byName(ResourceLocation texture) {
		return wrappers.computeIfAbsent(texture, key -> new WrappedTexture(Minecraft.getInstance()
				.getTextureManager()
				.getTexture(key)));
	}

	/**
	 * Get the overlay texture.
	 *
	 * @return The overlay texture.
	 */
	public Texture overlay() {
		return overlayTexture;
	}

	/**
	 * Get the light texture.
	 *
	 * @return The light texture.
	 */
	public Texture light() {
		return lightTexture;
	}

	public record WrappedTexture(AbstractTexture texture) implements IdentifiedTexture {
		@Override
		public void filter(boolean blur, boolean mipmap) {
			texture.setFilter(blur, mipmap);
		}

		@Override
		public int id() {
			return texture.getId();
		}
	}

	public record DirectTexture(int id) implements IdentifiedTexture {
		@Override
		public void filter(boolean blur, boolean mipmap) {
			// no-op
		}
	}
}
