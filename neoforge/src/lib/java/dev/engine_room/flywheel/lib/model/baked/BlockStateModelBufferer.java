package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
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
		List<BlockModelPart> parts = objects.parts;
		RandomSource random = objects.random;
		NeoForgeMeshEmitterManager emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		long seed = state.getSeed(pos);
		random.setSeed(seed);
		model.collectParts(level, pos, state, random, parts);

		// See ModelBlockRenderer#tesselateBlock
		boolean useAo = Minecraft.useAmbientOcclusion();
		boolean perPartAo = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
		boolean ao = useAo && (perPartAo || parts.isEmpty() || switch(parts.getFirst().ambientOcclusion()) {
			case TRUE -> true;
			case DEFAULT -> state.getLightEmission(level, pos) == 0;
			case FALSE -> false;
		});
		emitters.prepareForModel(ao);

		poseStack.pushPose();
		Minecraft.getInstance()
				.getBlockRenderer()
				.getModelRenderer()
				.tesselateBlock(level, parts, state, pos, poseStack, emitters, false, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		List<BlockModelPart> parts = objects.parts;
		RandomSource random = objects.random;
		NeoForgeMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance()
				.getBlockRenderer();
		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
		ModelBlockRenderer.enableCaching();

		boolean useAo = Minecraft.useAmbientOcclusion();
		boolean perPartAo = NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			emitters.prepareForBlock();

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					ChunkSectionLayer layer = ItemBlockRenderTypes.getRenderLayer(fluidState);

					BufferBuilder bufferBuilder = emitters.getEmitter(layer).getBuffer(true, false);

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
				model.collectParts(level, pos, state, random, parts);

				// See ModelBlockRenderer#tesselateBlock
				boolean ao = useAo && (perPartAo || parts.isEmpty() || switch(parts.getFirst().ambientOcclusion()) {
					case TRUE -> true;
					case DEFAULT -> state.getLightEmission(level, pos) == 0;
					case FALSE -> false;
				});
				emitters.prepareForModel(ao);

				poseStack.pushPose();
				poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
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
		public final List<BlockModelPart> parts = new ArrayList<>();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();

		public final NeoForgeMeshEmitterManager emitters = new NeoForgeMeshEmitterManager();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
