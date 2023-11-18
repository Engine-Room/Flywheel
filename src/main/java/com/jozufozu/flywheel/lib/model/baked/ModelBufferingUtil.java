package com.jozufozu.flywheel.lib.model.baked;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public final class ModelBufferingUtil {
	private static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);
	private static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

	private static final ThreadLocal<ModelBufferingObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ModelBufferingObjects::new);

	public static void bufferSingle(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, @Nullable PoseStack poseStack, IModelData modelData, ResultConsumer resultConsumer) {
		ModelBufferingObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		Random random = objects.random;
		BufferBuilder[] buffers = objects.shadedBuffers;

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];

			if (!ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
				continue;
			}

			BufferBuilder buffer = buffers[layerIndex];
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			ForgeHooksClient.setRenderType(renderType);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, buffer, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData);
			poseStack.popPose();

			buffer.end();
			Pair<DrawState, ByteBuffer> data = buffer.popNextBuffer();
			resultConsumer.accept(renderType, data);
		}

		ForgeHooksClient.setRenderType(null);
	}

	public static void bufferSingleShadeSeparated(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, @Nullable PoseStack poseStack, IModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		ModelBufferingObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		Random random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder[] shadedBuffers = objects.shadedBuffers;
		BufferBuilder[] unshadedBuffers = objects.unshadedBuffers;

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];

			if (!ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
				continue;
			}

			BufferBuilder shadedBuffer = shadedBuffers[layerIndex];
			BufferBuilder unshadedBuffer = unshadedBuffers[layerIndex];
			shadeSeparatingWrapper.prepare(shadedBuffer, unshadedBuffer);
			shadedBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			unshadedBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			ForgeHooksClient.setRenderType(renderType);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, shadeSeparatingWrapper, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData);
			poseStack.popPose();

			shadedBuffer.end();
			unshadedBuffer.end();
			Pair<DrawState, ByteBuffer> shadedData = shadedBuffer.popNextBuffer();
			Pair<DrawState, ByteBuffer> unshadedData = unshadedBuffer.popNextBuffer();
			resultConsumer.accept(renderType, true, shadedData);
			resultConsumer.accept(renderType, false, unshadedData);
		}

		ForgeHooksClient.setRenderType(null);

		shadeSeparatingWrapper.clear();
	}

	public static void bufferBlock(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, @Nullable PoseStack poseStack, IModelData modelData, ResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingle(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, modelData, resultConsumer);
	}

	public static void bufferBlockShadeSeparated(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, @Nullable PoseStack poseStack, IModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingleShadeSeparated(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, modelData, resultConsumer);
	}

	public static void bufferMultiBlock(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, @Nullable PoseStack poseStack, Map<BlockPos, IModelData> modelDataMap, ResultConsumer resultConsumer) {
		ModelBufferingObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		Random random = objects.random;

		BufferBuilder[] buffers = objects.shadedBuffers;
		for (BufferBuilder buffer : buffers) {
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			BlockState state = blockInfo.state;

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BakedModel model = renderDispatcher.getBlockModel(state);
			BlockPos pos = blockInfo.pos;
			long seed = state.getSeed(pos);
			IModelData modelData = modelDataMap.getOrDefault(pos, EmptyModelData.INSTANCE);

			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				RenderType renderType = CHUNK_LAYERS[layerIndex];

				if (!ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
					continue;
				}

				BufferBuilder buffer = buffers[layerIndex];

				ForgeHooksClient.setRenderType(renderType);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, buffer, true, random, seed, OverlayTexture.NO_OVERLAY, modelData);
				poseStack.popPose();
			}
		}

		ForgeHooksClient.setRenderType(null);
		ModelBlockRenderer.clearCache();

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];
			BufferBuilder buffer = buffers[layerIndex];
			buffer.end();
			Pair<DrawState, ByteBuffer> data = buffer.popNextBuffer();
			resultConsumer.accept(renderType, data);
		}
	}

	public static void bufferMultiBlockShadeSeparated(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, @Nullable PoseStack poseStack, Map<BlockPos, IModelData> modelDataMap, ShadeSeparatedResultConsumer resultConsumer) {
		ModelBufferingObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		Random random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;

		BufferBuilder[] shadedBuffers = objects.shadedBuffers;
		BufferBuilder[] unshadedBuffers = objects.unshadedBuffers;
		for (BufferBuilder buffer : shadedBuffers) {
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}
		for (BufferBuilder buffer : unshadedBuffers) {
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			BlockState state = blockInfo.state;

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BakedModel model = renderDispatcher.getBlockModel(state);
			BlockPos pos = blockInfo.pos;
			long seed = state.getSeed(pos);
			IModelData modelData = modelDataMap.getOrDefault(pos, EmptyModelData.INSTANCE);

			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				RenderType renderType = CHUNK_LAYERS[layerIndex];

				if (!ItemBlockRenderTypes.canRenderInLayer(state, renderType)) {
					continue;
				}

				shadeSeparatingWrapper.prepare(shadedBuffers[layerIndex], unshadedBuffers[layerIndex]);

				ForgeHooksClient.setRenderType(renderType);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, shadeSeparatingWrapper, true, random, seed, OverlayTexture.NO_OVERLAY, modelData);
				poseStack.popPose();
			}
		}

		ForgeHooksClient.setRenderType(null);
		ModelBlockRenderer.clearCache();

		shadeSeparatingWrapper.clear();

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];
			BufferBuilder shadedBuffer = shadedBuffers[layerIndex];
			BufferBuilder unshadedBuffer = unshadedBuffers[layerIndex];
			shadedBuffer.end();
			unshadedBuffer.end();
			Pair<DrawState, ByteBuffer> shadedData = shadedBuffer.popNextBuffer();
			Pair<DrawState, ByteBuffer> unshadedData = unshadedBuffer.popNextBuffer();
			resultConsumer.accept(renderType, true, shadedData);
			resultConsumer.accept(renderType, false, unshadedData);
		}
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, Pair<DrawState, ByteBuffer> data);
	}

	public interface ShadeSeparatedResultConsumer {
		void accept(RenderType renderType, boolean shaded, Pair<DrawState, ByteBuffer> data);
	}

	private static class ModelBufferingObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final Random random = new Random();

		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();

		public final BufferBuilder[] shadedBuffers = new BufferBuilder[CHUNK_LAYER_AMOUNT];
		public final BufferBuilder[] unshadedBuffers = new BufferBuilder[CHUNK_LAYER_AMOUNT];

		{
			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				int initialSize = CHUNK_LAYERS[layerIndex].bufferSize();
				shadedBuffers[layerIndex] = new BufferBuilder(initialSize);
				unshadedBuffers[layerIndex] = new BufferBuilder(initialSize);
			}
		}
	}
}
