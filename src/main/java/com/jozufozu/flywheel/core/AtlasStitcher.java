package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

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

	public void onTextureStitch(TextureStitchEvent.Pre event) {
		if (!event.getMap()
				.location()
				.equals(PlayerContainer.BLOCK_ATLAS))
			return;

		sprites.stream()
				.map(StitchedSprite::getLoc)
				.forEach(event::addSprite);
	}
}
