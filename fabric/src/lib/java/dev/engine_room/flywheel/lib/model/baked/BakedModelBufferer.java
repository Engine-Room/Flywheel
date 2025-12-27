package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

final class BakedModelBufferer {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BakedModelBufferer() {
	}

	public static SimpleModel bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		FabricMeshEmitterManager emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		ChunkSectionLayer defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);
		boolean useAo = Minecraft.useAmbientOcclusion();
		// See ModelBlockRenderer#tesselateBlock
		boolean defaultAo = useAo && state.getLightEmission() == 0 && model.useAmbientOcclusion();
		model = emitters.prepareForModel(model, defaultLayer, useAo, defaultAo);

		poseStack.pushPose();
		Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer()
				.tesselateBlock(level, model, state, pos, poseStack, emitters, false, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;
		FabricMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

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
				BlockStateModel model = renderDispatcher.getBlockModel(state);

				ChunkSectionLayer defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);

				// See ModelBlockRenderer#tesselateBlock
				boolean defaultAo = useAo && state.getLightEmission() == 0 && ((BlockModelPart) parts.getFirst()).useAmbientOcclusion();
				model = emitters.prepareForModel(model, defaultLayer, useAo, defaultAo);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
				List<BlockModelPart> parts = model.collectParts(random);
				blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitters, true, OverlayTexture.NO_OVERLAY);
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

		public final FabricMeshEmitterManager emitters = new FabricMeshEmitterManager();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
