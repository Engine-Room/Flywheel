package com.jozufozu.flywheel.lib.model;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import com.dreizak.miniball.highdim.Miniball;
import com.dreizak.miniball.model.PointSet;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.api.vertex.VertexViewProviderRegistry;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.vertex.PosVertexView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public final class ModelUtil {
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = createVanillaRenderer();

	private ModelUtil() {
	}

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
			LOGGER.error("Failed to initialize vanilla BlockRenderDispatcher!", e);
			return defaultDispatcher;
		}
		return dispatcher;
	}

	public static MemoryBlock convertVanillaBuffer(BufferBuilder.RenderedBuffer buffer, VertexView vertexView) {
		DrawState drawState = buffer.drawState();
		int vertexCount = drawState.vertexCount();
		VertexFormat srcFormat = drawState.format();

		ByteBuffer src = buffer.vertexBuffer();
		MemoryBlock dst = MemoryBlock.malloc((long) vertexCount * vertexView.stride());
		long srcPtr = MemoryUtil.memAddress(src);
		long dstPtr = dst.ptr();

		VertexView srcView = VertexViewProviderRegistry.getProvider(srcFormat).createVertexView();
		srcView.ptr(srcPtr);
		vertexView.ptr(dstPtr);
		srcView.vertexCount(vertexCount);
		vertexView.vertexCount(vertexCount);

		srcView.writeAll(vertexView);

		return dst;
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

	public static int computeTotalVertexCount(Iterable<Mesh> meshes) {
		int vertexCount = 0;
		for (Mesh mesh : meshes) {
			vertexCount += mesh.vertexCount();
		}
		return vertexCount;
	}

	public static Vector4f computeBoundingSphere(Iterable<Mesh> meshes) {
		int vertexCount = computeTotalVertexCount(meshes);
		var block = MemoryBlock.malloc((long) vertexCount * PosVertexView.STRIDE);
		var vertexList = new PosVertexView();

		int baseVertex = 0;
		for (Mesh mesh : meshes) {
			vertexList.ptr(block.ptr() + (long) baseVertex * PosVertexView.STRIDE);
			mesh.write(vertexList);
			baseVertex += mesh.vertexCount();
		}

		vertexList.ptr(block.ptr());
		vertexList.vertexCount(vertexCount);
		var sphere = computeBoundingSphere(vertexList);

		block.free();

		return sphere;
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
			public double coord(int i, int dim) {
				return switch (dim) {
					case 0 -> vertexList.x(i);
					case 1 -> vertexList.y(i);
					case 2 -> vertexList.z(i);
					default -> throw new IllegalArgumentException("Invalid dimension: " + dim);
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
