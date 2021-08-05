package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback.Registry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * This is primarily for hacking entity textures into the block atlas.
 */
public class AtlasStitcher {
	protected static final AtlasStitcher INSTANCE = new AtlasStitcher();

	public static AtlasStitcher getInstance() {
		return INSTANCE;
	}

	private final List<StitchedSprite> sprites = new ArrayList<>();

	public StitchedSprite get(ResourceLocation loc) {
		StitchedSprite sprite = new StitchedSprite(loc);

		sprites.add(sprite);

		return sprite;
	}

	public void onTextureStitch(TextureAtlas atlasTexture, Registry registry) {
		sprites.forEach(StitchedSprite::reset);
		sprites.stream()
				.map(StitchedSprite::getLoc)
				.forEach(registry::register);
	}
}
