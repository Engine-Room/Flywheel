package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.fabric.model.CullingBakedModel;
import com.jozufozu.flywheel.fabric.model.DefaultLayerFilteringBakedModel;
import com.jozufozu.flywheel.fabric.model.FabricModelUtil;
import com.jozufozu.flywheel.fabric.model.LayerFilteringBakedModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ModelUtil {
	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		model = DefaultLayerFilteringBakedModel.wrap(model);
		blockRenderer.tesselateBlock(VirtualEmptyBlockGetter.INSTANCE, model, referenceState, BlockPos.ZERO, ms, builder,
				false, new Random(), 42, OverlayTexture.NO_OVERLAY);
		builder.end();
		return builder;
	}

	public static BufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();

		PoseStack ms = new PoseStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(512);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL)
				continue;

			BakedModel model = dispatcher.getBlockModel(state);
			if (((FabricBakedModel) model).isVanillaAdapter()) {
				if (!FabricModelUtil.doesLayerMatch(state, layer)) {
					continue;
				}
			} else {
				model = CullingBakedModel.wrap(model);
				model = LayerFilteringBakedModel.wrap(model, layer);
			}

			BlockPos pos = info.pos;

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(renderWorld, model, state, pos, ms, builder,
					true, random, 42, OverlayTexture.NO_OVERLAY);
			ms.popPose();
		}
		ModelBlockRenderer.clearCache();

		builder.end();
		return builder;
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack.cast(stack)
					.centre()
					.rotateToFace(facing.getOpposite())
					.unCentre();
			return stack;
		};
	}
}
