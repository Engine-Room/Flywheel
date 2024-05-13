package com.jozufozu.flywheel.lib.model.baked;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
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

final class BakedModelBufferer {
	static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);
	static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBufferer() {
	}

	public static void bufferSingle(ModelBlockRenderer blockRenderer, BlockAndTintGetter level, BakedModel model, BlockState state, @Nullable PoseStack poseStack, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		MeshEmitter[] emitters = objects.emitters;
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;

		for (MeshEmitter emitter : emitters) {
			emitter.prepare(resultConsumer);
		}

		RenderType defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);
		universalEmitter.prepare(defaultLayer);
		model = universalEmitter.wrapModel(model);

		poseStack.pushPose();
		blockRenderer.tesselateBlock(level, model, state, BlockPos.ZERO, poseStack, universalEmitter, false, random, 42L, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();

		universalEmitter.clear();

		for (MeshEmitter emitter : emitters) {
			emitter.end();
		}
	}

	public static void bufferBlock(BlockRenderDispatcher renderDispatcher, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		bufferSingle(renderDispatcher.getModelRenderer(), level, renderDispatcher.getBlockModel(state), state, poseStack, resultConsumer);
	}

	public static void bufferMultiBlock(BlockRenderDispatcher renderDispatcher, Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		MeshEmitter[] emitters = objects.emitters;
		Reference2ReferenceMap<RenderType, MeshEmitter> emitterMap = objects.emitterMap;
		UniversalMeshEmitter universalEmitter = objects.universalEmitter;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		for (MeshEmitter emitter : emitters) {
			emitter.prepare(resultConsumer);
		}

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

					transformingWrapper.prepare(emitterMap.get(renderType).getBuffer(true), poseStack);

					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BakedModel model = renderDispatcher.getBlockModel(state);

				RenderType defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);
				universalEmitter.prepare(defaultLayer);
				model = universalEmitter.wrapModel(model);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(level, model, state, pos, poseStack, universalEmitter, true, random, seed, OverlayTexture.NO_OVERLAY);
				poseStack.popPose();
			}
		}

		ModelBlockRenderer.clearCache();
		transformingWrapper.clear();
		universalEmitter.clear();

		for (MeshEmitter emitter : emitters) {
			emitter.end();
		}
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, boolean shaded, RenderedBuffer data);
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
		public final Reference2ReferenceMap<RenderType, MeshEmitter> emitterMap = new Reference2ReferenceOpenHashMap<>();
		public final UniversalMeshEmitter universalEmitter;
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();

		{
			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				RenderType renderType = CHUNK_LAYERS[layerIndex];
				MeshEmitter emitter = new MeshEmitter(renderType);
				emitters[layerIndex] = emitter;
				emitterMap.put(renderType, emitter);
			}
			universalEmitter = new UniversalMeshEmitter(emitterMap);
		}
	}
}
