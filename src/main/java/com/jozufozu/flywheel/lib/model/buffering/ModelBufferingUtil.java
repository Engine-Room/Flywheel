package com.jozufozu.flywheel.lib.model.buffering;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
	private static final int CHUNK_LAYERS_AMOUNT = CHUNK_LAYERS.length;

	public static <T extends VertexConsumer> void bufferSingle(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, PoseStack poseStack, BufferFactory<T> bufferFactory, BufferWrapper<T> bufferWrapper, Random random, IModelData modelData, ResultConsumer<? super T> resultConsumer) {
		bufferWrapper.bufferFactory = bufferFactory;

		for (RenderType type : CHUNK_LAYERS) {
			if (!ItemBlockRenderTypes.canRenderInLayer(state, type)) {
				continue;
			}

			bufferWrapper.currentRenderType = type;
			bufferWrapper.delegate = null;

			ForgeHooksClient.setRenderType(type);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, bufferWrapper, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData);
			poseStack.popPose();

			T buffer = bufferWrapper.delegate;
			if (buffer != null) {
				resultConsumer.accept(type, buffer);
			}
		}

		ForgeHooksClient.setRenderType(null);

		bufferWrapper.bufferFactory = null;
		bufferWrapper.currentRenderType = null;
		bufferWrapper.delegate = null;
	}

	public static <T extends VertexConsumer> void bufferSingleShadeSeparated(ModelBlockRenderer blockRenderer, BlockAndTintGetter renderWorld, BakedModel model, BlockState state, PoseStack poseStack, ShadeSeparatedBufferFactory<T> bufferFactory, ShadeSeparatingBufferWrapper<T> bufferWrapper, Random random, IModelData modelData, ShadeSeparatedResultConsumer<? super T> resultConsumer) {
		bufferWrapper.bufferFactory = bufferFactory;

		for (RenderType type : CHUNK_LAYERS) {
			if (!ItemBlockRenderTypes.canRenderInLayer(state, type)) {
				continue;
			}

			bufferWrapper.currentRenderType = type;
			bufferWrapper.shadedConsumer = null;
			bufferWrapper.unshadedConsumer = null;

			ForgeHooksClient.setRenderType(type);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, bufferWrapper, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData);
			poseStack.popPose();

			T shadedConsumer = bufferWrapper.shadedConsumer;
			T unshadedConsumer = bufferWrapper.unshadedConsumer;
			if (shadedConsumer != null) {
				resultConsumer.accept(type, true, shadedConsumer);
			}
			if (unshadedConsumer != null) {
				resultConsumer.accept(type, false, unshadedConsumer);
			}
		}

		ForgeHooksClient.setRenderType(null);

		bufferWrapper.bufferFactory = null;
		bufferWrapper.currentRenderType = null;
		bufferWrapper.shadedConsumer = null;
		bufferWrapper.unshadedConsumer = null;
	}

	public static <T extends VertexConsumer> void bufferBlock(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, PoseStack poseStack, BufferFactory<T> bufferFactory, BufferWrapper<T> bufferWrapper, Random random, IModelData modelData, ResultConsumer<? super T> resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingle(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, bufferFactory, bufferWrapper, random, modelData, resultConsumer);
	}

	public static <T extends VertexConsumer> void bufferBlockShadeSeparated(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, PoseStack poseStack, ShadeSeparatedBufferFactory<T> bufferFactory, ShadeSeparatingBufferWrapper<T> bufferWrapper, Random random, IModelData modelData, ShadeSeparatedResultConsumer<? super T> resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingleShadeSeparated(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, bufferFactory, bufferWrapper, random, modelData, resultConsumer);
	}

	public static <T extends VertexConsumer> void bufferMultiBlock(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, PoseStack poseStack, BufferFactory<T> bufferFactory, BufferWrapper<T> bufferWrapper, Random random, Map<BlockPos, IModelData> modelDataMap, ResultConsumer<? super T> resultConsumer) {
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		bufferWrapper.bufferFactory = bufferFactory;
		@SuppressWarnings("unchecked")
		T[] bufferCache = (T[]) new VertexConsumer[CHUNK_LAYERS_AMOUNT];

		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			BlockState state = blockInfo.state;

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BakedModel model = renderDispatcher.getBlockModel(state);
			BlockPos pos = blockInfo.pos;
			long seed = state.getSeed(pos);
			IModelData modelData = modelDataMap.getOrDefault(pos, EmptyModelData.INSTANCE);

			for (int layerIndex = 0; layerIndex < CHUNK_LAYERS_AMOUNT; layerIndex++) {
				RenderType type = CHUNK_LAYERS[layerIndex];

				if (!ItemBlockRenderTypes.canRenderInLayer(state, type)) {
					continue;
				}

				bufferWrapper.currentRenderType = type;
				bufferWrapper.delegate = bufferCache[layerIndex];

				ForgeHooksClient.setRenderType(type);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, bufferWrapper, true, random, seed, OverlayTexture.NO_OVERLAY, modelData);
				poseStack.popPose();

				bufferCache[layerIndex] = bufferWrapper.delegate;
			}
		}

		ForgeHooksClient.setRenderType(null);
		ModelBlockRenderer.clearCache();

		bufferWrapper.bufferFactory = null;
		bufferWrapper.currentRenderType = null;
		bufferWrapper.delegate = null;

		for (int layerIndex = 0; layerIndex < CHUNK_LAYERS_AMOUNT; layerIndex++) {
			T buffer = bufferCache[layerIndex];
			if (buffer != null) {
				resultConsumer.accept(CHUNK_LAYERS[layerIndex], buffer);
			}
		}
	}

	public static <T extends VertexConsumer> void bufferMultiBlockShadeSeparated(Collection<StructureTemplate.StructureBlockInfo> blocks, BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, PoseStack poseStack, ShadeSeparatedBufferFactory<T> bufferFactory, ShadeSeparatingBufferWrapper<T> bufferWrapper, Random random, Map<BlockPos, IModelData> modelDataMap, ShadeSeparatedResultConsumer<? super T> resultConsumer) {
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		bufferWrapper.bufferFactory = bufferFactory;
		@SuppressWarnings("unchecked")
		T[] bufferCache = (T[]) new VertexConsumer[CHUNK_LAYERS_AMOUNT * 2];

		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			BlockState state = blockInfo.state;

			if (state.getRenderShape() != RenderShape.MODEL) {
				continue;
			}

			BakedModel model = renderDispatcher.getBlockModel(state);
			BlockPos pos = blockInfo.pos;
			long seed = state.getSeed(pos);
			IModelData modelData = modelDataMap.getOrDefault(pos, EmptyModelData.INSTANCE);

			for (int layerIndex = 0; layerIndex < CHUNK_LAYERS_AMOUNT; layerIndex++) {
				RenderType type = CHUNK_LAYERS[layerIndex];

				if (!ItemBlockRenderTypes.canRenderInLayer(state, type)) {
					continue;
				}

				bufferWrapper.currentRenderType = type;
				bufferWrapper.shadedConsumer = bufferCache[layerIndex];
				bufferWrapper.unshadedConsumer = bufferCache[layerIndex + CHUNK_LAYERS_AMOUNT];

				ForgeHooksClient.setRenderType(type);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, bufferWrapper, true, random, seed, OverlayTexture.NO_OVERLAY, modelData);
				poseStack.popPose();

				bufferCache[layerIndex] = bufferWrapper.shadedConsumer;
				bufferCache[layerIndex + CHUNK_LAYERS_AMOUNT] = bufferWrapper.unshadedConsumer;
			}
		}

		ForgeHooksClient.setRenderType(null);
		ModelBlockRenderer.clearCache();

		bufferWrapper.bufferFactory = null;
		bufferWrapper.currentRenderType = null;
		bufferWrapper.shadedConsumer = null;
		bufferWrapper.unshadedConsumer = null;

		for (int layerIndex = 0; layerIndex < CHUNK_LAYERS_AMOUNT; layerIndex++) {
			RenderType type = CHUNK_LAYERS[layerIndex];
			T shadedConsumer = bufferCache[layerIndex];
			T unshadedConsumer = bufferCache[layerIndex + CHUNK_LAYERS_AMOUNT];
			if (shadedConsumer != null) {
				resultConsumer.accept(type, true, shadedConsumer);
			}
			if (unshadedConsumer != null) {
				resultConsumer.accept(type, false, unshadedConsumer);
			}
		}
	}

	public interface BufferFactory<T extends VertexConsumer> {
		T get(RenderType renderType);
	}

	public interface ShadeSeparatedBufferFactory<T extends VertexConsumer> {
		T get(RenderType renderType, boolean shaded);
	}

	public interface ResultConsumer<T extends VertexConsumer> {
		void accept(RenderType renderType, T vertexConsumer);
	}

	public interface ShadeSeparatedResultConsumer<T extends VertexConsumer> {
		void accept(RenderType renderType, boolean shaded, T vertexConsumer);
	}

	public static class BufferWrapper<T extends VertexConsumer> implements LazyDelegatingVertexConsumer<T> {
		protected BufferFactory<T> bufferFactory;
		protected RenderType currentRenderType;
		protected T delegate;

		@Override
		public T getDelegate() {
			if (delegate == null) {
				delegate = bufferFactory.get(currentRenderType);
			}
			return delegate;
		}
	}

	public static class ShadeSeparatingBufferWrapper<T extends VertexConsumer> implements ShadeSeparatingVertexConsumer<T> {
		protected ShadeSeparatedBufferFactory<T> bufferFactory;
		protected RenderType currentRenderType;
		protected T shadedConsumer;
		protected T unshadedConsumer;

		@Override
		public T getShadedConsumer() {
			if (shadedConsumer == null) {
				shadedConsumer = bufferFactory.get(currentRenderType, true);
			}
			return shadedConsumer;
		}

		@Override
		public T getUnshadedConsumer() {
			if (unshadedConsumer == null) {
				unshadedConsumer = bufferFactory.get(currentRenderType, false);
			}
			return unshadedConsumer;
		}
	}
}
