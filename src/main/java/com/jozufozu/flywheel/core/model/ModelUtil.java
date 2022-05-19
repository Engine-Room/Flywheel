package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Random;

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

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return getBufferBuilder(VirtualEmptyBlockGetter.INSTANCE, model, referenceState, poseStack);
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BlockAndTintGetter renderWorld, BakedModel model, BlockState referenceState, PoseStack poseStack) {
		ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);
		model = DefaultLayerFilteringBakedModel.wrap(model);
		model = shadeSeparatingWrapper.wrapModel(model);
		blockRenderer.tesselateBlock(renderWorld, model, referenceState, BlockPos.ZERO, poseStack, shadeSeparatingWrapper,
				false, objects.random, 42, OverlayTexture.NO_OVERLAY);
		shadeSeparatingWrapper.clear();
		unshadedBuilder.end();
		builder.appendUnshadedVertices(unshadedBuilder);
		builder.end();

		return builder;
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return getBufferBuilderFromTemplate(renderWorld, layer, blocks, new PoseStack());
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, PoseStack poseStack) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();	
		ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		Random random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);

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
				model = shadeSeparatingWrapper.wrapModel(model);
			}

			BlockPos pos = info.pos;

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, shadeSeparatingWrapper,
					true, random, 42, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
		ModelBlockRenderer.clearCache();

		shadeSeparatingWrapper.clear();
		unshadedBuilder.end();
		builder.appendUnshadedVertices(unshadedBuilder);
		builder.end();

		return builder;
	}

	private static PoseStack createRotation(Direction facing) {
		PoseStack stack = new PoseStack();
		TransformStack.cast(stack)
				.centre()
				.rotateToFace(facing.getOpposite())
				.unCentre();
		return stack;
	}

	public static PoseStack rotateToFace(Direction facing) {
		return TRANSFORMS.get(facing);
	}

	private static final EnumMap<Direction, PoseStack> TRANSFORMS = new EnumMap<>(Direction.class);

	static {
		for (Direction value : Direction.values()) {
			TRANSFORMS.put(value, createRotation(value));
		}
	}

    private static class ThreadLocalObjects {
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}
}
