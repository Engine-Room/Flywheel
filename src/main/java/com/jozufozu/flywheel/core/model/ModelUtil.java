package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ModelUtil {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public static ShadeSeparatedBufferBuilder getBufferBuilder(Bufferable bufferable) {
		ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		objects.begin();

		bufferable.bufferInto(blockRenderer, objects.shadeSeparatingWrapper, objects.random);

		objects.end();

		return objects.separatedBufferBuilder;
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return new BakedModelBuilder(model).withReferenceState(referenceState)
				.withPoseStack(poseStack)
				.build();
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(BlockAndTintGetter renderWorld, BakedModel model, BlockState referenceState, PoseStack poseStack) {
		return new BakedModelBuilder(model).withReferenceState(referenceState)
				.withPoseStack(poseStack)
				.withRenderWorld(renderWorld)
				.build();
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		return new WorldModelBuilder(layer).withRenderWorld(renderWorld)
				.withBlocks(blocks)
				.build();
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks, PoseStack poseStack) {
		return new WorldModelBuilder(layer).withRenderWorld(renderWorld)
				.withBlocks(blocks)
				.withPoseStack(poseStack)
				.build();
	}

	public static Supplier<PoseStack> rotateToFace(Direction facing) {
		return () -> {
			PoseStack stack = new PoseStack();
			TransformStack.cast(stack)
					.centre()
					.rotateToFace(facing.getOpposite())
					.unCentre();
			return stack;
		};
	}

	private static class ThreadLocalObjects {
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final ShadeSeparatedBufferBuilder separatedBufferBuilder = new ShadeSeparatedBufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);

		private void begin() {
			this.separatedBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			this.unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			this.shadeSeparatingWrapper.prepare(this.separatedBufferBuilder, this.unshadedBuilder);
		}

		private void end() {
			this.shadeSeparatingWrapper.clear();
			this.unshadedBuilder.end();
			this.separatedBufferBuilder.appendUnshadedVertices(this.unshadedBuilder);
			this.separatedBufferBuilder.end();
		}
	}
}
