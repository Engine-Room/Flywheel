package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public final class WorldModelBuilder implements Bufferable {
	private final RenderType layer;

	private PoseStack poseStack = new PoseStack();
	private Map<BlockPos, IModelData> modelData = Collections.emptyMap();
	private BlockAndTintGetter renderWorld = VirtualEmptyBlockGetter.INSTANCE;
	private Collection<StructureTemplate.StructureBlockInfo> blocks = Collections.emptyList();

	public WorldModelBuilder(RenderType layer) {
		this.layer = layer;
	}

	@Override
	public void bufferInto(ModelBlockRenderer modelRenderer, VertexConsumer consumer, Random random) {
		ForgeHooksClient.setRenderType(this.layer);
		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : this.blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL) continue;
			if (!ItemBlockRenderTypes.canRenderInLayer(state, this.layer)) continue;

			BlockPos pos = info.pos;

			IModelData data = this.modelData.getOrDefault(pos, EmptyModelData.INSTANCE);

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(this.renderWorld, ModelUtil.VANILLA_RENDERER.getBlockModel(state), state, pos, poseStack, consumer, true, random, 42, OverlayTexture.NO_OVERLAY, data);
			poseStack.popPose();
		}
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);
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

	public WorldModelBuilder withModelData(Map<BlockPos, IModelData> modelData) {
		this.modelData = modelData;
		return this;
	}

	public WorldModelBuilder withPoseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public WorldModel intoMesh(String name) {
		return new WorldModel(ModelUtil.getBufferBuilder(this), name);
	}
}
