
package com.jozufozu.flywheel.core.model;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BakedModelBuilder implements Bufferable {
	private final BakedModel model;
	private BlockAndTintGetter renderWorld = VirtualEmptyBlockGetter.INSTANCE;
	private BlockState referenceState = Blocks.AIR.defaultBlockState();
	private PoseStack poseStack = new PoseStack();

	public BakedModelBuilder(BakedModel model) {
		this.model = model;
	}

	public BakedModelBuilder withRenderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public BakedModelBuilder withReferenceState(BlockState referenceState) {
		this.referenceState = referenceState;
		return this;
	}

	public BakedModelBuilder withPoseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	@Override
	public void bufferInto(ModelBlockRenderer blockRenderer, VertexConsumer consumer, RandomSource random) {
		blockRenderer.tesselateBlock(renderWorld, model, referenceState, BlockPos.ZERO, poseStack, consumer, false, random, 42, OverlayTexture.NO_OVERLAY, ModelUtil.VIRTUAL_DATA, null);
	}
}
