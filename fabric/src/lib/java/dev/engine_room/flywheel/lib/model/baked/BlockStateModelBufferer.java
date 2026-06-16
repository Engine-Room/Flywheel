package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@ApiStatus.Internal
public final class BlockStateModelBufferer {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private BlockStateModelBufferer() {
	}

	public static SimpleModel bufferModel(BlockStateModel model, BlockPos pos, BlockAndTintGetter level, BlockState state, @Nullable PoseStack poseStack, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		FabricMeshEmitterManager emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		long seed = state.getSeed(pos);

		Minecraft minecraft = Minecraft.getInstance();
		GameRenderState gameRenderState = minecraft.gameRenderer.getGameRenderState();
		BlockColors blockColors = minecraft.getBlockColors();

		// See ModelBlockRenderer#tesselateBlock
		boolean useAo = gameRenderState.optionsRenderState.ambientOcclusion;
		boolean defaultAo = useAo && state.getLightEmission() == 0;
		emitters.prepareForModel(useAo, defaultAo);

		AltModelBlockRenderer altModelBlockRenderer = Renderer.get().altModelBlockRenderer(useAo, true, blockColors);

		PoseStack finalPoseStack = poseStack;
		QuadEmitter quadEmitter = Renderer.get().quadEmitter((quad) -> {
			finalPoseStack.pushPose();
			ChunkSectionLayer layer = quad.chunkLayer();
			FabricMeshEmitter emitter = emitters.getEmitter(layer);
			emitters.prepareForGeometry(quad);
			quad.buffer(OverlayTexture.NO_OVERLAY, finalPoseStack.last(), emitter);
			finalPoseStack.popPose();
		});

		altModelBlockRenderer.tesselateBlock(quadEmitter, 0, 0, 0, level, pos, state, model, seed);

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		FabricMeshEmitterManager emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		BlockModelLighter.enableCaching();

		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		GameRenderState gameRenderState = minecraft.gameRenderer.getGameRenderState();
		boolean useAo = gameRenderState.optionsRenderState.ambientOcclusion;
		BlockColors blockColors = minecraft.getBlockColors();

		AltModelBlockRenderer altModelBlockRenderer = Renderer.get().altModelBlockRenderer(useAo, true, blockColors);
		FluidRenderer fluidRenderer = new FluidRenderer(modelManager.getFluidStateModelSet());

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
				BlockStateModel model = modelManager.getBlockStateModelSet().get(state);

				// See ModelBlockRenderer#tesselateBlock
				boolean defaultAo = useAo && state.getLightEmission() == 0;
				emitters.prepareForModel(useAo, defaultAo);

				PoseStack finalPoseStack = poseStack;
				QuadEmitter quadEmitter = Renderer.get().quadEmitter((quad) -> {
					finalPoseStack.pushPose();
					ChunkSectionLayer layer = quad.chunkLayer();
					FabricMeshEmitter emitter = emitters.getEmitter(layer);
					emitters.prepareForGeometry(quad);
					quad.buffer(OverlayTexture.NO_OVERLAY, finalPoseStack.last(), emitter);
					finalPoseStack.popPose();
				});
				altModelBlockRenderer.tesselateBlock(quadEmitter, pos.getX(), pos.getY(), pos.getZ(), level, pos, state, model, seed);
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
