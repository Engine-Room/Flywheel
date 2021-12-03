package com.jozufozu.flywheel.core.crumbling;

import static org.lwjgl.opengl.GL20.glUniform2f;

import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.shader.WorldProgram;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class CrumblingProgram extends WorldProgram {
	protected final int uTextureScale;
	protected int uCrumbling;

	public CrumblingProgram(ResourceLocation name, int handle) {
		super(name, handle);

		uTextureScale = getUniformLocation("uTextureScale");
	}

	@Override
	protected void registerSamplers() {
		super.registerSamplers();
		uCrumbling = setSamplerBinding("uCrumbling", 4);
	}

	public void setTextureScale(float x, float y) {
		glUniform2f(uTextureScale, x, y);
	}

	public void setAtlasSize(int width, int height) {
		TextureAtlas blockAtlas = AtlasInfo.getAtlas(InventoryMenu.BLOCK_ATLAS);
		if (blockAtlas == null) return;

		TextureAtlasSprite sprite = blockAtlas.getSprite(ModelBakery.BREAKING_LOCATIONS.get(0));

		setTextureScale(width / (float) sprite.getWidth(), height / (float) sprite.getHeight());
	}

}
