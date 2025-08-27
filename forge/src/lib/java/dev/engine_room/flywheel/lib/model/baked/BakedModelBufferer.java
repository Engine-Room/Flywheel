package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

final class BakedModelBufferer {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBufferer() {
	}

	public static void bufferModel(BakedModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, ModelData modelData, ResultConsumer<?> resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		ForgeMeshEmitterManager emitters = objects.emitters;

		emitters.prepare(resultConsumer);

		ModelBlockRenderer blockRenderer = Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer();

		long seed = state.getSeed(pos);
		modelData = model.getModelData(level, pos, state, modelData);
		random.setSeed(seed);
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

		boolean defaultAo = Minecraft.useAmbientOcclusion() && state.getLightEmission(level, pos) == 0;

		for (RenderType renderType : renderTypes) {
			boolean defaultAoLayer = defaultAo && model.useAmbientOcclusion(state, renderType);

			ForgeMeshEmitter emitter = emitters.getEmitter(renderType);
			emitter.prepareForModelLayer(defaultAoLayer);

			poseStack.pushPose();
			blockRenderer.tesselateBlock(level, model, state, pos, poseStack, emitter, false, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
			poseStack.popPose();
		}

		emitters.end();
	}

	public static void bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, Function<BlockPos, ModelData> modelDataLookup, boolean renderFluids, ResultConsumer<?> resultConsumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		ForgeMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(resultConsumer);

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance()
				.getBlockRenderer();
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		boolean useAo = Minecraft.useAmbientOcclusion();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			emitters.prepareForBlock();

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

					BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

					if (bufferBuilder != null) {
						transformingWrapper.prepare(bufferBuilder, poseStack);

						poseStack.pushPose();
						poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
						renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
						poseStack.popPose();
					}
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BakedModel model = renderDispatcher.getBlockModel(state);
				ModelData modelData = modelDataLookup.apply(pos);
				modelData = model.getModelData(level, pos, state, modelData);
				random.setSeed(seed);
				ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, random, modelData);

				boolean defaultAo = useAo && state.getLightEmission(level, pos) == 0;

				for (RenderType renderType : renderTypes) {
					boolean defaultAoLayer = defaultAo && model.useAmbientOcclusion(state, renderType);

					ForgeMeshEmitter emitter = emitters.getEmitter(renderType);
					emitter.prepareForModelLayer(defaultAoLayer);

					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					blockRenderer.tesselateBlock(level, model, state, pos, poseStack, emitter, true, random, seed, OverlayTexture.NO_OVERLAY, modelData, renderType);
					poseStack.popPose();
				}
			}
		}

		ModelBlockRenderer.clearCache();
		transformingWrapper.clear();
		emitters.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final ForgeMeshEmitterManager emitters = new ForgeMeshEmitterManager();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
