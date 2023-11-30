package com.jozufozu.flywheel.lib.model.baked;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

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
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public final class BakedModelBufferer {
	private static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);
	private static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBufferer() {
	}

	public static void bufferSingle(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		BufferBuilder[] buffers = objects.shadedBuffers;

		modelData = model.getModelData(renderWorld, BlockPos.ZERO, state, modelData);
		random.setSeed(42L);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		for (RenderType renderType : renderTypes) {
			int layerIndex = renderType.getChunkLayerId();

			BufferBuilder buffer = buffers[layerIndex];
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, buffer, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();

			RenderedBuffer data = buffer.endOrDiscardIfEmpty();
			if (data != null) {
				resultConsumer.accept(renderType, data);
				data.release();
			}
		}
	}

	public static void bufferSingleShadeSeparated(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder[] shadedBuffers = objects.shadedBuffers;
		BufferBuilder[] unshadedBuffers = objects.unshadedBuffers;

		modelData = model.getModelData(renderWorld, BlockPos.ZERO, state, modelData);
		random.setSeed(42L);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		for (RenderType renderType : renderTypes) {
			int layerIndex = renderType.getChunkLayerId();

			BufferBuilder shadedBuffer = shadedBuffers[layerIndex];
			BufferBuilder unshadedBuffer = unshadedBuffers[layerIndex];
			shadeSeparatingWrapper.prepare(shadedBuffer, unshadedBuffer);
			shadedBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			unshadedBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, shadeSeparatingWrapper, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();

			RenderedBuffer shadedData = shadedBuffer.endOrDiscardIfEmpty();
			if (shadedData != null) {
				resultConsumer.accept(renderType, true, shadedData);
				shadedData.release();
			}
			RenderedBuffer unshadedData = unshadedBuffer.endOrDiscardIfEmpty();
			if (unshadedData != null) {
				resultConsumer.accept(renderType, false, unshadedData);
				unshadedData.release();
			}
		}

		shadeSeparatingWrapper.clear();
	}

	public static void bufferBlock(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingle(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, modelData, resultConsumer);
	}

	public static void bufferBlockShadeSeparated(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ShadeSeparatedResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingleShadeSeparated(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, modelData, resultConsumer);
	}

	public static void bufferMultiBlock(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, @Nullable PoseStack poseStack, Map<BlockPos, ModelData> modelDataMap, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;

		BufferBuilder[] buffers = objects.shadedBuffers;
		for (BufferBuilder buffer : buffers) {
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			BlockState state = blockInfo.state();

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BlockPos pos = blockInfo.pos();
			long seed = state.getSeed(pos);
			BakedModel model = renderDispatcher.getBlockModel(state);
			ModelData modelData = modelDataMap.getOrDefault(pos, ModelData.EMPTY);
			modelData = model.getModelData(renderWorld, pos, state, modelData);
			random.setSeed(seed);
			ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

			for (RenderType renderType : renderTypes) {
				int layerIndex = renderType.getChunkLayerId();

				BufferBuilder buffer = buffers[layerIndex];

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, buffer, true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
				poseStack.popPose();
			}
		}

		ModelBlockRenderer.clearCache();

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];
			BufferBuilder buffer = buffers[layerIndex];
			RenderedBuffer data = buffer.endOrDiscardIfEmpty();
			if (data != null) {
				resultConsumer.accept(renderType, data);
				data.release();
			}
		}
	}

	public static void bufferMultiBlockShadeSeparated(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, @Nullable PoseStack poseStack, Map<BlockPos, ModelData> modelDataMap, ShadeSeparatedResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
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
			BlockState state = blockInfo.state();

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BlockPos pos = blockInfo.pos();
			long seed = state.getSeed(pos);
			BakedModel model = renderDispatcher.getBlockModel(state);
			ModelData modelData = modelDataMap.getOrDefault(pos, ModelData.EMPTY);
			modelData = model.getModelData(renderWorld, pos, state, modelData);
			random.setSeed(seed);
			ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

			for (RenderType renderType : renderTypes) {
				int layerIndex = renderType.getChunkLayerId();

				shadeSeparatingWrapper.prepare(shadedBuffers[layerIndex], unshadedBuffers[layerIndex]);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, shadeSeparatingWrapper, true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
				poseStack.popPose();
			}
		}

		ModelBlockRenderer.clearCache();

		shadeSeparatingWrapper.clear();

		for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
			RenderType renderType = CHUNK_LAYERS[layerIndex];
			BufferBuilder shadedBuffer = shadedBuffers[layerIndex];
			BufferBuilder unshadedBuffer = unshadedBuffers[layerIndex];
			RenderedBuffer shadedData = shadedBuffer.endOrDiscardIfEmpty();
			if (shadedData != null) {
				resultConsumer.accept(renderType, true, shadedData);
				shadedData.release();
			}
			RenderedBuffer unshadedData = unshadedBuffer.endOrDiscardIfEmpty();
			if (unshadedBuffer != null) {
				resultConsumer.accept(renderType, false, unshadedData);
				unshadedData.release();
			}
		}
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, RenderedBuffer data);
	}

	public interface ShadeSeparatedResultConsumer {
		void accept(RenderType renderType, boolean shaded, RenderedBuffer data);
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

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
