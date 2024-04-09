package com.jozufozu.flywheel.vanilla.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * blaze3d doesn't forward #putBulkData, but we want that for our MeshEmitter
 */
@Mixin(targets = "com.mojang.blaze3d.vertex.VertexMultiConsumer$Double")
public abstract class VertexMultiConsumerDoubleMixin implements VertexConsumer {

	@Shadow
	@Final
	private VertexConsumer first;

	@Shadow
	@Final
	private VertexConsumer second;

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
		first.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay, readExistingColor);
		second.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay, readExistingColor);
	}

	@Override
	public void putBulkData(PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, float alpha, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
		first.putBulkData(pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, alpha, pCombinedLights, pCombinedOverlay, pMulColor);
		second.putBulkData(pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, alpha, pCombinedLights, pCombinedOverlay, pMulColor);
	}
}
