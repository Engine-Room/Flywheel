package com.jozufozu.flywheel.core.crumbling;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * Track width and height of all created texture atlases.
 *
 * @see com.jozufozu.flywheel.mixin.atlas.AtlasDataMixin
 */
public class AtlasInfo {

	private static final Map<ResourceLocation, SheetSize> sheetData = new HashMap<>();

	@Nullable
	public static TextureAtlas getAtlas(ResourceLocation name) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(name);

		if (texture instanceof TextureAtlas atlas)
			return atlas;
		else
			return null;
	}

	@Nullable
	public static SheetSize getSheetSize(@Nullable ResourceLocation loc) {
		return sheetData.get(loc);
	}

	/**
	 * FOR USE IN MIXIN
	 */
	public static void _setAtlasData(ResourceLocation atlas, SpriteLoader.Preparations preparations) {
		sheetData.put(atlas, new SheetSize(preparations.width(), preparations.height()));
	}

	public record SheetSize(int width, int height) {
	}
}
