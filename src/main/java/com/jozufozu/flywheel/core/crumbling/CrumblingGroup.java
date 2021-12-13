package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterialGroup;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterialRenderer;
import com.jozufozu.flywheel.core.atlas.AtlasInfo;
import com.jozufozu.flywheel.core.atlas.SheetData;
import com.jozufozu.flywheel.util.RenderTextures;
import com.jozufozu.flywheel.util.TextureBinder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.RenderType;

public class CrumblingGroup<P extends CrumblingProgram> extends InstancedMaterialGroup<P> {

	private int width;
	private int height;

	public CrumblingGroup(InstancingEngine<P> owner, RenderType type) {
		super(owner, type);
	}

	@Override
	public void render(Matrix4f viewProjection, double camX, double camY, double camZ) {
		type.setupRenderState();

		int renderTex = RenderSystem.getShaderTexture(0);

		updateAtlasSize();

		type.clearRenderState();

		CrumblingRenderer._currentLayer.setupRenderState();

		int breakingTex = RenderSystem.getShaderTexture(0);

		RenderSystem.setShaderTexture(0, renderTex);
		RenderSystem.setShaderTexture(4, breakingTex);

		TextureBinder.bindActiveTextures();
		for (InstancedMaterialRenderer<P> renderer : renderers) {
			renderer.render(viewProjection, camX, camY, camZ);
		}

		CrumblingRenderer._currentLayer.clearRenderState();
	}

	private void updateAtlasSize() {

		SheetData atlasData = AtlasInfo.getAtlasData(RenderTextures.getShaderTexture(0));

		if (atlasData != null) {
			width = atlasData.width;
			height = atlasData.height;
		} else {
			width = height = 256;
		}
	}

	@Override
	public void setup(P p) {
		p.setAtlasSize(width, height);
	}
}
