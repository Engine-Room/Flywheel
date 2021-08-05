package com.jozufozu.flywheel.core.crumbling;

import static org.lwjgl.opengl.GL20.glUniform2f;

import java.util.List;

import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;

import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;

public class CrumblingProgram extends WorldProgram {
	protected final int uTextureScale;
	protected int uCrumbling;

	public CrumblingProgram(Program program, List<IProgramExtension> extensions) {
		super(program, extensions);

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
		AtlasTexture blockAtlas = AtlasInfo.getAtlas(PlayerContainer.BLOCK_ATLAS);
		if (blockAtlas == null) return;

		TextureAtlasSprite sprite = blockAtlas.getSprite(ModelBakery.BREAKING_LOCATIONS.get(0));

		setTextureScale(width / (float) sprite.getWidth(), height / (float) sprite.getHeight());
	}

}
