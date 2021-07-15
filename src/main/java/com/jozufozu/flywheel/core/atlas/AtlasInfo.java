package com.jozufozu.flywheel.core.atlas;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.mixin.atlas.SheetDataAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public class AtlasInfo {

	private static final Map<ResourceLocation, SheetData> sheetData = new HashMap<>();

	public static AtlasTexture getAtlas(ResourceLocation name) {
		Texture texture = Minecraft.getInstance().textureManager.getTexture(name);

		if (texture instanceof AtlasTexture)
			return (AtlasTexture) texture;
		else
			return null;
	}

	public static SheetData getAtlasData(TextureAtlasSprite texture) {
		return getAtlasData(texture.atlas());
	}

	public static SheetData getAtlasData(AtlasTexture atlas) {
		return getAtlasData(atlas.location());
	}

	public static SheetData getAtlasData(ResourceLocation loc) {
		return sheetData.get(loc);
	}

	public static void setAtlasData(ResourceLocation atlas, SheetDataAccessor accessor) {
		sheetData.put(atlas, new SheetData(accessor.getWidth(), accessor.getHeight()));
	}

}
