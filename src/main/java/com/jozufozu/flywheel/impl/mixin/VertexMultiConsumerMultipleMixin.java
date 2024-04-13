package com.jozufozu.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;

@Mixin(targets = "com.mojang.blaze3d.vertex.VertexMultiConsumer$Multiple")
public abstract class VertexMultiConsumerMultipleMixin implements VertexConsumer {
	@Shadow
	@Final
	private VertexConsumer[] delegates;

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
		for (VertexConsumer delegate : this.delegates) {
			delegate.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay, readExistingColor);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, float alpha, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
		for (VertexConsumer delegate : this.delegates) {
			delegate.putBulkData(pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, alpha, pCombinedLights, pCombinedOverlay, pMulColor);
		}
	}
}
