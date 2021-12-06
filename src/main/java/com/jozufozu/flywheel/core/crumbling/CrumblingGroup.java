package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.material.MaterialGroupImpl;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;
import com.jozufozu.flywheel.backend.material.MaterialRenderer;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;
import com.jozufozu.flywheel.util.RenderTextures;
import com.jozufozu.flywheel.util.TextureBinder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CrumblingGroup<P extends CrumblingProgram> extends MaterialGroupImpl<P> {

	private int width;
	private int height;

	public CrumblingGroup(MaterialManagerImpl<P> owner) {
		super(owner);
	}

	@Override
	public void render(RenderType type, Matrix4f viewProjection, double camX, double camY, double camZ) {
		type.setupRenderState();

		int renderTex = RenderSystem.getShaderTexture(0);

		ResourceLocation texture = RenderTextures.getShaderTexture(0);

		if (texture != null) {
			SheetData atlasData = AtlasInfo.getAtlasData(texture);

			width = atlasData.width;
			height = atlasData.height;
		} else {
			width = height = 256;
		}

		type.clearRenderState();

		CrumblingRenderer._currentLayer.setupRenderState();

		int breakingTex = RenderSystem.getShaderTexture(0);

		RenderSystem.setShaderTexture(0, renderTex);
		RenderSystem.setShaderTexture(4, breakingTex);

		TextureBinder.bindActiveTextures();
		for (MaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ);
		}

		CrumblingRenderer._currentLayer.clearRenderState();
	}

	@Override
	public void setup(P p) {
		p.setAtlasSize(width, height);
	}
}
