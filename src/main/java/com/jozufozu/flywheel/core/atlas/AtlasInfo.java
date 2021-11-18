package com.jozufozu.flywheel.core.atlas;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.mixin.atlas.SheetDataAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class AtlasInfo {

	private static final Map<ResourceLocation, SheetData> sheetData = new HashMap<>();

	public static TextureAtlas getAtlas(ResourceLocation name) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(name);

		if (texture instanceof TextureAtlas)
			return (TextureAtlas) texture;
		else
			return null;
	}

	public static SheetData getAtlasData(TextureAtlasSprite texture) {
		return getAtlasData(texture.atlas());
	}

	public static SheetData getAtlasData(TextureAtlas atlas) {
		return getAtlasData(atlas.location());
	}

	public static SheetData getAtlasData(ResourceLocation loc) {
		return sheetData.get(loc);
	}

	public static void setAtlasData(ResourceLocation atlas, SheetDataAccessor accessor) {
		sheetData.put(atlas, new SheetData(accessor.getWidth(), accessor.getHeight()));
	}

}
