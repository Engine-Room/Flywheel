package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

final class BakedModelBufferer {
	static final RenderType[] CHUNK_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);
	static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBufferer() {
	}

	public static void bufferSingle(@Nullable BlockAndTintGetter level, BakedModel model, @Nullable BlockState state, @Nullable PoseStack poseStack, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (level == null) {
			if (state == null) {
				state = Blocks.AIR.defaultBlockState();
			}
			OriginBlockAndTintGetter originLevel = objects.level;
			originLevel.originBlockState(state);
			level = originLevel;
		} else if (state == null) {
			state = level.getBlockState(BlockPos.ZERO);
		}
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		MeshEmitter[] emitters = objects.emitters;

		ModelData modelData = model.getModelData(level, BlockPos.ZERO, state, level.getModelData(BlockPos.ZERO));
		random.setSeed(42L);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		ModelBlockRenderer blockRenderer = Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer();

		for (RenderType renderType : renderTypes) {
			int layerIndex = renderType.getChunkLayerId();
			MeshEmitter emitter = emitters[layerIndex];

			emitter.prepare(resultConsumer);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(level, model, state, BlockPos.ZERO, poseStack, emitter, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();

			emitter.end();
		}
	}

	public static void bufferBlock(@Nullable BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ResultConsumer resultConsumer) {
		if (state.getRenderShape() != RenderShape.MODEL) {
			return;
		}

		var blockModel = Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state);
		bufferSingle(level, blockModel, state, poseStack, resultConsumer);
	}

	public static void bufferMultiBlock(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, ResultConsumer resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		MeshEmitter[] emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		for (MeshEmitter emitter : emitters) {
			emitter.prepare(resultConsumer);
		}

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance()
				.getBlockRenderer();

		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
					int layerIndex = renderType.getChunkLayerId();

					transformingWrapper.prepare(emitters[layerIndex].unwrap(true), poseStack);

					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BakedModel model = renderDispatcher.getBlockModel(state);
				ModelData modelData = model.getModelData(level, pos, state, level.getModelData(pos));
				random.setSeed(seed);
				ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

				for (RenderType renderType : renderTypes) {
					int layerIndex = renderType.getChunkLayerId();

					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					blockRenderer.tesselateBlock(level, model, state, pos, poseStack, emitters[layerIndex], true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
					poseStack.popPose();
				}
			}
		}

		ModelBlockRenderer.clearCache();
		transformingWrapper.clear();

		for (MeshEmitter emitter : emitters) {
			emitter.end();
		}
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, boolean shaded, MeshData data);
	}

	private static class ThreadLocalObjects {
		public final OriginBlockAndTintGetter level = new OriginBlockAndTintGetter(p -> 0, p -> 0);
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();

		{
			for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
				RenderType renderType = CHUNK_LAYERS[layerIndex];
				emitters[layerIndex] = new MeshEmitter(renderType);
			}
		}
	}
}
