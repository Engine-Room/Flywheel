package com.jozufozu.flywheel.lib.model.baked;

import java.util.Iterator;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

final class BakedModelBufferer {
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
		var consumers = objects.emitters;

		modelData = model.getModelData(renderWorld, BlockPos.ZERO, state, modelData);
		random.setSeed(42L);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		for (RenderType renderType : renderTypes) {
			int layerIndex = renderType.getChunkLayerId();
			var consumer = consumers[layerIndex];

			consumer.begin(resultConsumer);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(renderWorld, model, state, BlockPos.ZERO, poseStack, consumer, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();

			consumer.end();
		}
	}

	public static void bufferBlock(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter renderWorld, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingle(renderDispatcher.getModelRenderer(), renderWorld, renderDispatcher.getBlockModel(state), state, poseStack, modelData, resultConsumer);
	}

	public static void bufferMultiBlock(BlockRenderDispatcher renderDispatcher, Iterator<BlockPos> posIterator, BlockAndTintGetter renderWorld, @Nullable PoseStack poseStack, Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		var emitters = objects.emitters;

		for (var emitter : emitters) {
			emitter.begin(resultConsumer);
		}

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = renderWorld.getBlockState(pos);

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
					int layerIndex = layer.getChunkLayerId();

					transformingWrapper.prepare(emitters[layerIndex], poseStack);

					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					renderDispatcher.renderLiquid(pos, renderWorld, transformingWrapper, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BakedModel model = renderDispatcher.getBlockModel(state);
				ModelData modelData = modelDataLookup.apply(pos);
				modelData = model.getModelData(renderWorld, pos, state, modelData);
				random.setSeed(seed);
				ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

				for (RenderType renderType : renderTypes) {
					int layerIndex = renderType.getChunkLayerId();

					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					blockRenderer.tesselateBlock(renderWorld, model, state, pos, poseStack, emitters[layerIndex], true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
					poseStack.popPose();
				}
			}
		}

		ModelBlockRenderer.clearCache();

		for (var emitter : emitters) {
			emitter.end();
		}

		transformingWrapper.clear();
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, boolean shaded, RenderedBuffer data);
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();

		public final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];

		{
			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				var renderType = CHUNK_LAYERS[layerIndex];
				// FIXME: We leak the memory owned by the BufferBuilder here.
				var buffer = new BufferBuilder(renderType.bufferSize());
				emitters[layerIndex] = new MeshEmitter(buffer, renderType);
			}
		}
	}
}
