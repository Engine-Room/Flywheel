package com.jozufozu.flywheel.core.crumbling;

import org.joml.Matrix4f;

import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterialGroup;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.util.Textures;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.RenderType;

public class CrumblingGroup<P extends CrumblingProgram> extends InstancedMaterialGroup<P> {

	private int width;
	private int height;

	public CrumblingGroup(InstancingEngine<P> owner, RenderType type) {
		super(owner, type);
	}

	// XXX See notes of overriden method
	@Override
	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, RenderLayer layer) {
		type.setupRenderState();

		int renderTex = RenderSystem.getShaderTexture(0);

		updateAtlasSize();

		type.clearRenderState();

		CrumblingRenderer._currentLayer.setupRenderState();

		int breakingTex = RenderSystem.getShaderTexture(0);

		RenderSystem.setShaderTexture(0, renderTex);
		RenderSystem.setShaderTexture(4, breakingTex);

		Textures.bindActiveTextures();
		renderAll(viewProjection, camX, camY, camZ, layer);

		CrumblingRenderer._currentLayer.clearRenderState();
	}

	private void updateAtlasSize() {

		AtlasInfo.SheetSize sheetSize = AtlasInfo.getSheetSize(Textures.getShaderTexture(0));

		if (sheetSize != null) {
			width = sheetSize.width();
			height = sheetSize.height();
		} else {
			width = height = 256;
		}
	}

	@Override
    protected void setup(P p) {
		p.setAtlasSize(width, height);
	}
}
