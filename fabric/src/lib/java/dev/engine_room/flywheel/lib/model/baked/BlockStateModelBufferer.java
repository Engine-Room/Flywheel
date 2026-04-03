package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

final class BlockStateModelBufferer {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BlockStateModelBufferer() {
	}

	public static SimpleModel bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		PoseStack finalPoseStack = poseStack;
		FabricMeshEmitterManager emitters = objects.emitters;
		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(Minecraft.getInstance().gameRenderer.getGameRenderState().optionsRenderState.ambientOcclusion, true, Minecraft.getInstance().getBlockColors());

		emitters.prepare(blockMaterialFunction);

		long seed = state.getSeed(pos);

		// See ModelBlockRenderer#tesselateBlock
		boolean useAo = Minecraft.getInstance().gameRenderer.getGameRenderState().optionsRenderState.ambientOcclusion;
		boolean defaultAo = useAo && state.getLightEmission() == 0;
		model = emitters.prepareForModel(model, useAo, defaultAo);

		blockRenderer.tesselateBlock((x, y, z, quad, instance) -> {
			finalPoseStack.pushPose();
			finalPoseStack.translate(x, y, z);
			emitters.getEmitter(quad.materialInfo().layer()).putBakedQuad(finalPoseStack.last(), quad, instance);
			finalPoseStack.popPose();
		}, 0, 0, 0, level, pos, state, model, seed);

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		PoseStack finalPoseStack = poseStack;

		FabricMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(Minecraft.getInstance().gameRenderer.getGameRenderState().optionsRenderState.ambientOcclusion, true, Minecraft.getInstance().getBlockColors());
		FluidRenderer fluidRenderer = new FluidRenderer(Minecraft.getInstance().getModelManager().getFluidStateModelSet());
		BlockModelLighter.enableCaching();

		boolean useAo = Minecraft.getInstance().gameRenderer.getGameRenderState().optionsRenderState.ambientOcclusion;

		while (posIterator.hasNext()) {
			BlockPos pos = posIterator.next();
			BlockState state = level.getBlockState(pos);

			emitters.prepareForBlock();

			if (renderFluids) {
				FluidState fluidState = state.getFluidState();

				if (!fluidState.isEmpty()) {
					poseStack.pushPose();
					poseStack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
					fluidRenderer.tesselate(level, pos, layer -> {
						BufferBuilder builder = emitters.getEmitter(layer).getBuffer(true, false);

						if (builder != null) {
							transformingWrapper.prepare(builder, finalPoseStack);
							return transformingWrapper;
						}

						return EmptyVertexConsumer.INSTANCE;
					}, state, fluidState);
					poseStack.popPose();
				}
			}

			if (state.getRenderShape() == RenderShape.MODEL) {
				long seed = state.getSeed(pos);
				BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);

				// See ModelBlockRenderer#tesselateBlock
				boolean defaultAo = useAo && state.getLightEmission() == 0;
				model = emitters.prepareForModel(model, useAo, defaultAo);

				blockRenderer.tesselateBlock((x, y, z, quad, instance) -> {
					finalPoseStack.pushPose();
					finalPoseStack.translate(x, y, z);
					emitters.getEmitter(quad.materialInfo().layer()).putBakedQuad(finalPoseStack.last(), quad, instance);
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

		public final FabricMeshEmitterManager emitters = new FabricMeshEmitterManager();
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
	}
}
