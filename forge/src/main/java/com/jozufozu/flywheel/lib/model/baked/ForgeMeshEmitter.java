package com.jozufozu.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

class ForgeMeshEmitter extends MeshEmitter {
	ForgeMeshEmitter(BufferBuilder bufferBuilder, RenderType renderType) {
		super(bufferBuilder, renderType);
	}

	// Forge has another putBulkData that we need to override
	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad quad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
		observeQuadAndEmitIfNecessary(quad);

		bufferBuilder.putBulkData(matrixEntry, quad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
	}
}
