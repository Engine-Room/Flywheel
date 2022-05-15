package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class SeparatedWorldModelBuilder {

	private PoseStack poseStack = new PoseStack();
	private Map<BlockPos, IModelData> modelData = Collections.emptyMap();
	private BlockAndTintGetter renderWorld = VirtualEmptyBlockGetter.INSTANCE;
	private Collection<StructureTemplate.StructureBlockInfo> blocks = Collections.emptyList();

	public Map<RenderType, Mesh> getMeshes() {
		Map<RenderType, BufferBuilder> builders = new HashMap<>();

		ModelBlockRenderer modelRenderer = ModelUtil.VANILLA_RENDERER.getModelRenderer();

		buffer(modelRenderer, new Random(), type -> builders.computeIfAbsent(type, $ -> {
			var out = new BufferBuilder(512);

			out.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			return out;
		}));

		return builders.entrySet()
				.stream()
				.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> {
					var b = e.getValue();

					b.end();

					return new BlockMesh(Formats.BLOCK.createReader(b), "");
				}));
	}

	public void buffer(ModelBlockRenderer modelRenderer, Random random, Function<RenderType, VertexConsumer> consumer) {
		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : this.blocks) {
			var state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL) continue;

			var pos = info.pos;
			var seed = state.getSeed(pos);
			var data = this.modelData.getOrDefault(pos, EmptyModelData.INSTANCE);
			var blockModel = ModelUtil.VANILLA_RENDERER.getBlockModel(state);

			this.poseStack.pushPose();
			this.poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

			for (RenderType type : RenderType.chunkBufferLayers()) {
				if (!ItemBlockRenderTypes.canRenderInLayer(state, type)) {
					continue;
				}

				var vertexConsumer = consumer.apply(type);

				if (vertexConsumer == null) {
					continue;
				}

				ForgeHooksClient.setRenderType(type);

				modelRenderer.tesselateBlock(this.renderWorld, blockModel, state, pos, poseStack, vertexConsumer, true, random, seed, OverlayTexture.NO_OVERLAY, data);
			}
			this.poseStack.popPose();
		}
		ForgeHooksClient.setRenderType(null);
		ModelBlockRenderer.clearCache();
	}

	public SeparatedWorldModelBuilder withRenderWorld(BlockAndTintGetter renderWorld) {
		this.renderWorld = renderWorld;
		return this;
	}

	public SeparatedWorldModelBuilder withBlocks(Collection<StructureTemplate.StructureBlockInfo> blocks) {
		this.blocks = blocks;
		return this;
	}

	public SeparatedWorldModelBuilder withModelData(Map<BlockPos, IModelData> modelData) {
		this.modelData = modelData;
		return this;
	}

	public SeparatedWorldModelBuilder withPoseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}
}
