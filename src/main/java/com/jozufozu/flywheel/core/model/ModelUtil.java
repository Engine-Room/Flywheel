package com.jozufozu.flywheel.core.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyModelData;
import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ModelUtil {
	private static final Lazy<ModelBlockRenderer> MODEL_RENDERER = Lazy.of(() -> new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()));

	// DOWN, UP, NORTH, SOUTH, WEST, EAST, null
	private static final Direction[] CULL_FACES;

	static {
		Direction[] directions = Direction.values();

		CULL_FACES = Arrays.copyOf(directions, directions.length + 1);
	}

	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		blockRenderer.tesselateBlock(VirtualEmptyBlockGetter.INSTANCE, model, referenceState, BlockPos.ZERO, ms, builder,
				false, new Random(), 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
		builder.end();
		return builder;
	}

	public static BufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		ModelBlockRenderer modelRenderer = MODEL_RENDERER.get();
		BlockModelShaper blockModels = Minecraft.getInstance().getModelManager().getBlockModelShaper();

		PoseStack ms = new PoseStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(512);
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

		ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL)
				continue;
			if (!ItemBlockRenderTypes.canRenderInLayer(state, layer))
				continue;

			BlockPos pos = info.pos;

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(renderWorld, blockModels.getBlockModel(state), state, pos, ms, builder,
					true, random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			ms.popPose();
		}
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);

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
