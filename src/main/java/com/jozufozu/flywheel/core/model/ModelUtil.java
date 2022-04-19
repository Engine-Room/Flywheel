package com.jozufozu.flywheel.core.model;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Random;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ModelUtil {
	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = createVanillaRenderer();

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private static BlockRenderDispatcher createVanillaRenderer() {
		BlockRenderDispatcher defaultDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcher dispatcher = new BlockRenderDispatcher(null, null, null);
		try {
			for (Field field : BlockRenderDispatcher.class.getDeclaredFields()) {
				field.setAccessible(true);
				field.set(dispatcher, field.get(defaultDispatcher));
			}
			ObfuscationReflectionHelper.setPrivateValue(BlockRenderDispatcher.class, dispatcher, new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()), "f_110900_");
		} catch (Exception e) {
			Flywheel.LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	public static BakedModelBuilder bakedModel(BakedModel model) {
		return new BakedModelBuilder(model);
	}

	public static WorldModelBuilder worldLayer(RenderType layer) {
		return new WorldModelBuilder(layer);
	}

	public static ShadeSeparatedBufferBuilder getBufferBuilder(Bufferable object) {
		ModelBlockRenderer blockRenderer = VANILLA_RENDERER.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		objects.unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		objects.shadeSeparatingWrapper.prepare(builder, objects.unshadedBuilder);

		object.bufferInto(blockRenderer, objects.shadeSeparatingWrapper, objects.random);

		objects.shadeSeparatingWrapper.clear();
		objects.unshadedBuilder.end();
		builder.appendUnshadedVertices(objects.unshadedBuilder);
		builder.end();

		return builder;
	}

	private static PoseStack createRotation(Direction facing) {
		PoseStack stack = new PoseStack();
		TransformStack.cast(stack)
				.centre()
				.rotateToFace(facing.getOpposite())
				.unCentre();
		return stack;
	}

	public static PoseStack rotateToFace(Direction facing) {
		return TRANSFORMS.get(facing);
	}

	private static final EnumMap<Direction, PoseStack> TRANSFORMS = new EnumMap<>(Direction.class);

	static {
		for (Direction value : Direction.values()) {
			TRANSFORMS.put(value, createRotation(value));
		}
	}

	private static class ThreadLocalObjects {
		public final Random random = new Random();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}

}
