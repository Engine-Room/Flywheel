package com.jozufozu.flywheel.lib.model;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.dreizak.miniball.highdim.Miniball;
import com.dreizak.miniball.model.PointSet;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.lib.format.Formats;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.VertexFormat;
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

	public static Pair<VertexType, MemoryBlock> convertBlockBuffer(Pair<DrawState, ByteBuffer> pair) {
		DrawState drawState = pair.getFirst();
		int vertexCount = drawState.vertexCount();
		VertexFormat srcFormat = drawState.format();
		VertexType dstVertexType = Formats.BLOCK;

		ByteBuffer src = pair.getSecond();
		MemoryBlock dst = MemoryBlock.malloc(vertexCount * dstVertexType.getLayout().getStride());
		long srcPtr = MemoryUtil.memAddress(src);
		long dstPtr = dst.ptr();

		ReusableVertexList srcList = VertexListProvider.get(srcFormat).createVertexList();
		ReusableVertexList dstList = dstVertexType.createVertexList();
		srcList.ptr(srcPtr);
		dstList.ptr(dstPtr);
		srcList.vertexCount(vertexCount);
		dstList.vertexCount(vertexCount);

		srcList.writeAll(dstList);

		return Pair.of(dstVertexType, dst);
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

	public static Vector4f computeBoundingSphere(VertexList vertexList) {
		return computeBoundingSphere(new PointSet() {
			@Override
			public int size() {
				return vertexList.vertexCount();
			}

			@Override
			public int dimension() {
				return 3;
			}

			@Override
			public double coord(int i, int j) {
				return switch (j) {
					case 0 -> vertexList.x(i);
					case 1 -> vertexList.y(i);
					case 2 -> vertexList.z(i);
					default -> throw new IllegalArgumentException("Invalid dimension: " + j);
				};
			}
		});
	}

	public static Vector4f computeBoundingSphere(PointSet points) {
		var miniball = new Miniball(points);
		double[] center = miniball.center();
		double radius = miniball.radius();
		return new Vector4f((float) center[0], (float) center[1], (float) center[2], (float) radius);
	}
}
