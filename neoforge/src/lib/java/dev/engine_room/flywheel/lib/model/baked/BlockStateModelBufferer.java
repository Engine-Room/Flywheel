package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
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
		RandomSource random = objects.random;
		MeshEmitterManager<NeoforgeMeshEmitter> emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		ModelBlockRenderer blockRenderer = Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer();

		List<BlockModelPart> parts = model.collectParts(level, pos, state, random);

		long seed = state.getSeed(pos);
		random.setSeed(seed);
		ChunkSectionLayer layer = parts.getFirst().getRenderType(state);

		// See ModelBlockRenderer#tesselateBlock
		boolean aoEnabled = Minecraft.useAmbientOcclusion();
		boolean perPartAO = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
		boolean defaultAo = state.getLightEmission(level, pos) == 0;
		TriState useAo = parts.getFirst().ambientOcclusion();

		boolean defaultAoLayer = aoEnabled && ((perPartAO && useAo.isTrue()) || (useAo.isDefault() && defaultAo));

		NeoforgeMeshEmitter emitter = emitters.getEmitter(layer);
		emitter.prepareForModelLayer(defaultAoLayer);

		poseStack.pushPose();
		blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, false, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		MeshEmitterManager<NeoforgeMeshEmitter> emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance()
				.getBlockRenderer();
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		boolean aoEnabled = Minecraft.useAmbientOcclusion();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			emitters.prepareForBlock();

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					ChunkSectionLayer layer = ItemBlockRenderTypes.getRenderLayer(fluidState);

					BufferBuilder bufferBuilder = emitters.getBuffer(layer, true, false);

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
				random.setSeed(seed);

				BlockStateModel model = renderDispatcher.getBlockModel(state);
				List<BlockModelPart> parts = model.collectParts(level, pos, state, random);

				ChunkSectionLayer layer = ItemBlockRenderTypes.getChunkRenderType(state);

				// See ModelBlockRenderer#tesselateBlock
				boolean perPartAO = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
				boolean defaultAo = state.getLightEmission(level, pos) == 0;
				TriState useAo = parts.getFirst().ambientOcclusion();

				boolean defaultAoLayer = aoEnabled && ((perPartAO && useAo.isTrue()) || (useAo.isDefault() && defaultAo));

				NeoforgeMeshEmitter emitter = emitters.getEmitter(layer);
				emitter.prepareForModelLayer(defaultAoLayer);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitter, true, OverlayTexture.NO_OVERLAY);
				poseStack.popPose();
			}
		}

		ModelBlockRenderer.clearCache();
		transformingWrapper.clear();
		return emitters.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final MeshEmitterManager<NeoforgeMeshEmitter> emitters = new MeshEmitterManager<>(NeoforgeMeshEmitter::new);
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
