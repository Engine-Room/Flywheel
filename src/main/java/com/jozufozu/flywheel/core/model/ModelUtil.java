package com.jozufozu.flywheel.core.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ModelUtil {
	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = createVanillaRenderer();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private static BlockRenderDispatcher createVanillaRenderer() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "f_110900_");
		} catch (Exception e) {
			Flywheel.LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return getBufferBuilder(VirtualEmptyBlockGetter.INSTANCE, model, referenceState, poseStack);
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BlockAndTintGetter renderWorld, BakedModel model, BlockState referenceState, PoseStack poseStack) {
		ModelBlockRenderer blockRenderer = VANILLA_RENDERER.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);
		blockRenderer.tesselateBlock(renderWorld, model, referenceState, BlockPos.ZERO, poseStack, shadeSeparatingWrapper,
				false, objects.random, 42, OverlayTexture.NO_OVERLAY, VirtualEmptyModelData.INSTANCE);
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
		ModelBlockRenderer modelRenderer = VANILLA_RENDERER.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		Random random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(builder, unshadedBuilder);

		ForgeHooksClient.setRenderType(layer);
		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL)
				continue;
			if (!ItemBlockRenderTypes.canRenderInLayer(state, layer))
				continue;

			BlockPos pos = info.pos;

			poseStack.pushPose();
			poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
			modelRenderer.tesselateBlock(renderWorld, VANILLA_RENDERER.getBlockModel(state), state, pos, poseStack, shadeSeparatingWrapper,
					true, random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			poseStack.popPose();
		}
		ModelBlockRenderer.clearCache();
		ForgeHooksClient.setRenderType(null);

		shadeSeparatingWrapper.clear();
		unshadedBuilder.end();
		builder.appendUnshadedVertices(unshadedBuilder);
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

	private static class ThreadLocalObjects {
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}
}
