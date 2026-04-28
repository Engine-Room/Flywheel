package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;

final class BlockStateModelBufferer {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BlockStateModelBufferer() {
	}

	public static SimpleModel bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		List<BlockStateModelPart> parts = objects.parts;
		RandomSource random = objects.random;
		NeoForgeMeshEmitterManager emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		long seed = state.getSeed(pos);
		random.setSeed(seed);
		model.collectParts(level, pos, state, random, parts);

		Minecraft minecraft = Minecraft.getInstance();
		GameRenderState gameRenderState = minecraft.gameRenderer.getGameRenderState();
		BlockColors blockColors = minecraft.getBlockColors();

		// See ModelBlockRenderer#tesselateBlock
		boolean useAo = gameRenderState.optionsRenderState.ambientOcclusion;
		boolean perPartAo = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
		boolean ao = useAo && (perPartAo || parts.isEmpty() || switch(parts.getFirst().ambientOcclusion()) {
			case TRUE -> true;
			case DEFAULT -> state.getLightEmission(level, pos) == 0;
			case FALSE -> false;
		});
		emitters.prepareForModel(ao);

		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(useAo, true, blockColors);

		PoseStack finalPoseStack = poseStack;
		blockRenderer.tesselateBlock((_, _, _, quad, instance) -> {
			finalPoseStack.pushPose();
			ChunkSectionLayer layer = quad.materialInfo().layer();
			emitters.getEmitter(layer).putBakedQuad(finalPoseStack.last(), quad, instance);
			finalPoseStack.popPose();
		}, 0, 0, 0, level, pos, state, model, seed);

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		List<BlockStateModelPart> parts = objects.parts;
		RandomSource random = objects.random;
		NeoForgeMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		BlockModelLighter.enableCaching();

		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		GameRenderState gameRenderState = minecraft.gameRenderer.getGameRenderState();
		boolean useAo = gameRenderState.optionsRenderState.ambientOcclusion;
		BlockColors blockColors = minecraft.getBlockColors();

		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(useAo, true, blockColors);
		FluidRenderer fluidRenderer = new FluidRenderer(modelManager.getFluidStateModelSet());

		boolean perPartAo = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			emitters.prepareForBlock();

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					PoseStack finalPoseStack = poseStack;
					fluidRenderer.tesselate(level, pos, layer -> {
						BufferBuilder bufferBuilder = emitters.getEmitter(layer).getBuffer(true, false);

						if (bufferBuilder != null) {
							transformingWrapper.prepare(bufferBuilder, finalPoseStack);
							return transformingWrapper;
						}

						return EmptyVertexConsumer.INSTANCE;
					}, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				random.setSeed(seed);

				BlockStateModel model = modelManager.getBlockStateModelSet().get(state);
				model.collectParts(level, pos, state, random, parts);

				// See ModelBlockRenderer#tesselateBlock
				boolean ao = useAo && (perPartAo || parts.isEmpty() || switch(parts.getFirst().ambientOcclusion()) {
					case TRUE -> true;
					case DEFAULT -> state.getLightEmission(level, pos) == 0;
					case FALSE -> false;
				});
				emitters.prepareForModel(ao);

				PoseStack finalPoseStack = poseStack;
				blockRenderer.tesselateBlock((x, y, z, quad, instance) -> {
					finalPoseStack.pushPose();
					finalPoseStack.translate(x, y, z);
					ChunkSectionLayer layer = quad.materialInfo().layer();
					emitters.getEmitter(layer).putBakedQuad(finalPoseStack.last(), quad, instance);
					finalPoseStack.popPose();
				}, pos.getX(), pos.getY(), pos.getZ(), level, pos, state, model, seed);
			}
		}

		BlockModelLighter.clearCache();
		transformingWrapper.clear();
		return emitters.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final List<BlockStateModelPart> parts = new ArrayList<>();
		public final RandomSource random = RandomSource.createThreadLocalInstance();

		public final NeoForgeMeshEmitterManager emitters = new NeoForgeMeshEmitterManager();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
