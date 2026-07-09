package dev.engine_room.flywheel.lib.model.baked;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
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
		PoseStack stack = poseStack;
		MeshEmitterManager<NeoforgeMeshEmitter> emitters = objects.emitters;

		emitters.prepare(blockMaterialFunction);

		Minecraft minecraft = Minecraft.getInstance();
		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(minecraft.options.ambientOcclusion().get(), false, minecraft.getBlockColors());
		long seed = state.getSeed(pos);
		boolean defaultAo = defaultAo(minecraft, level, pos, state);
		BlockQuadOutput output = outputFor(emitters, stack, false, defaultAo);

		BlockModelLighter.enableCaching();
		try {
			blockRenderer.tesselateBlock(output, 0, 0, 0, level, pos, state, model, seed);
		} finally {
			BlockModelLighter.clearCache();
		}

		return emitters.end();
	}

	public static SimpleModel bufferBlocks(Iterator<BlockPos> posIterator, BlockAndTintGetter level, @Nullable PoseStack poseStack, boolean renderFluids, BlockMaterialFunction blockMaterialFunction) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		PoseStack stack = poseStack;
		MeshEmitterManager<NeoforgeMeshEmitter> emitters = objects.emitters;
		TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

		emitters.prepare(blockMaterialFunction);

		Minecraft minecraft = Minecraft.getInstance();
		ModelBlockRenderer blockRenderer = new ModelBlockRenderer(minecraft.options.ambientOcclusion().get(), true, minecraft.getBlockColors());
		FluidRenderer fluidRenderer = new FluidRenderer(minecraft.getModelManager().getFluidStateModelSet());

		BlockModelLighter.enableCaching();
		try {
			while (posIterator.hasNext()) {
				BlockPos pos = posIterator.next();
				BlockState state = level.getBlockState(pos);

				emitters.prepareForBlock();

				if (renderFluids) {
					FluidState fluidState = state.getFluidState();

					if (!fluidState.isEmpty()) {
						FluidRenderer.Output fluidOutput = layer -> {
							BufferBuilder bufferBuilder = emitters.getBuffer(layer, true, false);
							if (bufferBuilder == null) {
								return objects.discardingVertexConsumer;
							}

							transformingWrapper.prepare(bufferBuilder, stack);
							return transformingWrapper;
						};

						stack.pushPose();
						stack.translate(pos.getX() - (pos.getX() & 0xF), pos.getY() - (pos.getY() & 0xF), pos.getZ() - (pos.getZ() & 0xF));
						var customRenderer = minecraft.getModelManager().getFluidStateModelSet().get(fluidState).customRenderer();
						if (customRenderer == null || !customRenderer.renderFluid(fluidRenderer, fluidState, level, pos, fluidOutput, state)) {
							fluidRenderer.tesselate(level, pos, fluidOutput, state, fluidState);
						}
						stack.popPose();
					}
				}

				if (state.getRenderShape() == RenderShape.MODEL) {
					long seed = state.getSeed(pos);
					BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(state);
					boolean forceSolid = ModelBlockRenderer.forceOpaque(minecraft.options.cutoutLeaves().get(), state);
					boolean defaultAo = defaultAo(minecraft, level, pos, state);
					BlockQuadOutput output = outputFor(emitters, stack, forceSolid, defaultAo);

					blockRenderer.tesselateBlock(output, pos.getX(), pos.getY(), pos.getZ(), level, pos, state, model, seed);
				}
			}
		} finally {
			BlockModelLighter.clearCache();
			transformingWrapper.clear();
		}

		return emitters.end();
	}

	private static boolean defaultAo(Minecraft minecraft, BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return minecraft.options.ambientOcclusion().get() && state.getLightEmission(level, pos) == 0;
	}

	private static BlockQuadOutput outputFor(MeshEmitterManager<NeoforgeMeshEmitter> emitters, PoseStack poseStack, boolean forceSolid, boolean defaultAo) {
		return (x, y, z, quad, instance) -> {
			ChunkSectionLayer layer = forceSolid ? ChunkSectionLayer.SOLID : quad.materialInfo().layer();
			NeoforgeMeshEmitter emitter = emitters.getEmitter(layer);
			emitter.put(poseStack, x, y, z, quad, instance, defaultAo);
		};
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();

		public final MeshEmitterManager<NeoforgeMeshEmitter> emitters = new MeshEmitterManager<>(NeoforgeMeshEmitter::new);
		public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
		public final VertexConsumer discardingVertexConsumer = new DiscardingVertexConsumer();
	}

	private static class DiscardingVertexConsumer implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			return this;
		}

		@Override
		public VertexConsumer setColor(int color) {
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			return this;
		}
	}
}
