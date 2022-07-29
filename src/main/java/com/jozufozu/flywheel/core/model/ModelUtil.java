package com.jozufozu.flywheel.core.model;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ModelUtil {
	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = createVanillaRenderer();

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

	public static VertexList createVertexList(BufferBuilder bufferBuilder) {
		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
		BufferBuilder.DrawState drawState = pair.getFirst();

		if (drawState.format() != DefaultVertexFormat.BLOCK) {
			throw new RuntimeException("Cannot use BufferBuilder with " + drawState.format());
		}

		return Formats.BLOCK.createReader(pair.getSecond(), drawState.vertexCount());
	}

	@Nullable
	public static Material getMaterial(RenderType chunkRenderType, boolean shaded) {
		if (chunkRenderType == RenderType.solid()) {
			return shaded ? Materials.CHUNK_SOLID_SHADED : Materials.CHUNK_SOLID_UNSHADED;
		}
		if (chunkRenderType == RenderType.cutoutMipped()) {
			return shaded ? Materials.CHUNK_CUTOUT_MIPPED_SHADED : Materials.CHUNK_CUTOUT_MIPPED_UNSHADED;
		}
		if (chunkRenderType == RenderType.cutout()) {
			return shaded ? Materials.CHUNK_CUTOUT_SHADED : Materials.CHUNK_CUTOUT_UNSHADED;
		}
		if (chunkRenderType == RenderType.translucent()) {
			return shaded ? Materials.CHUNK_TRANSLUCENT_SHADED : Materials.CHUNK_TRANSLUCENT_UNSHADED;
		}
		if (chunkRenderType == RenderType.tripwire()) {
			return shaded ? Materials.CHUNK_TRIPWIRE_SHADED : Materials.CHUNK_TRIPWIRE_UNSHADED;
		}
		return null;
	}
}
