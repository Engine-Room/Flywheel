package com.jozufozu.flywheel.backend.engine.textures;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.context.Texture;
import com.jozufozu.flywheel.api.context.TextureSource;
import com.jozufozu.flywheel.backend.mixin.LightTextureAccessor;
import com.jozufozu.flywheel.backend.mixin.OverlayTextureAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public class TextureSourceImpl implements TextureSource {

	private final DirectTexture lightTexture;
	private final DirectTexture overlayTexture;
	private final Map<ResourceLocation, WrappedTexture> wrappers = new HashMap<>();

	public TextureSourceImpl() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		this.lightTexture = new DirectTexture(((LightTextureAccessor) gameRenderer.lightTexture()).flywheel$texture()
				.getId());
		this.overlayTexture = new DirectTexture(((OverlayTextureAccessor) gameRenderer.overlayTexture()).flywheel$texture()
				.getId());
	}


	@Override
	public Texture byName(ResourceLocation texture) {
		return wrappers.computeIfAbsent(texture, key -> new WrappedTexture(Minecraft.getInstance()
				.getTextureManager()
				.getTexture(key)));
	}

	@Override
	public Texture overlay() {
		return overlayTexture;
	}

	@Override
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
