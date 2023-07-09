package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.Collections;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.fabric.model.LayerFilteringBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class WorldModelBuilder implements Bufferable {
	private final RenderType layer;

	private PoseStack poseStack = new PoseStack();
	private BlockAndTintGetter renderWorld = VirtualEmptyBlockGetter.INSTANCE;
	private Collection<StructureTemplate.StructureBlockInfo> blocks = Collections.emptyList();

	public WorldModelBuilder(RenderType layer) {
		this.layer = layer;
	}

	@Override
	public void bufferInto(VertexConsumer consumer, ModelBlockRenderer modelRenderer, RandomSource random) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : this.blocks) {
			BlockState state = info.state();
			if (state.getRenderShape() != RenderShape.MODEL) continue;

			BakedModel model = dispatcher.getBlockModel(state);
			if (model.isVanillaAdapter()) {
				if (ItemBlockRenderTypes.getChunkRenderType(state) != layer) {
					continue;
				}
			} else {
				model = LayerFilteringBakedModel.wrap(model, layer);
			}
			if (consumer instanceof ShadeSeparatingVertexConsumer shadeSeparatingWrapper) {
				model = shadeSeparatingWrapper.wrapModel(model);
			}

			BlockPos pos = info.pos();

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(this.renderWorld, model, state, pos, poseStack, consumer, true, random, 42, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
		ModelBlockRenderer.clearCache();
	}

	/**
	 * It is expected that {@code renderWorld.getShade(...)} returns a constant.
	 */
	public WorldModelBuilder withRenderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public WorldModelBuilder withBlocks(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		this.blocks = blocks;
		return this;
	}

	public WorldModelBuilder withPoseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModel toModel(String name) {
		return BlockModel.of(this, name);
	}
}
