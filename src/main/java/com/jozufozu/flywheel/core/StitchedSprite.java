package com.jozufozu.flywheel.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class StitchedSprite {

	private final ResourceLocation loc;

	TextureAtlasSprite sprite;

	StitchedSprite(ResourceLocation loc) {
		this.loc = loc;
	}

	public ResourceLocation getLoc() {
		return loc;
	}

	public TextureAtlasSprite getSprite() {
		if (sprite == null) {
			sprite = Minecraft.getInstance()
					.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
					.apply(loc);
		}

		return sprite;
	}

	void reset() {
		sprite = null;
	}
}
