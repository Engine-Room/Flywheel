package com.jozufozu.flywheel.lib.model;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.internal.FlywheelLibPlatform;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.api.vertex.VertexViewProviderRegistry;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.vertex.PosVertexView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;

public final class ModelUtil {
	/**
	 * An alternative BlockRenderDispatcher that circumvents the Forge rendering pipeline to ensure consistency.
	 * Meant to be used for virtual rendering.
	 */
	public static final BlockRenderDispatcher VANILLA_RENDERER = FlywheelLibPlatform.INSTANCE.createVanillaRenderer();
	private static final float BOUNDING_SPHERE_EPSILON = 1e-4f;

	private ModelUtil() {
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

	public static Vector4f computeBoundingSphere(Collection<Model.ConfiguredMesh> meshes) {
		return computeBoundingSphere(meshes.stream().map(Model.ConfiguredMesh::mesh).toList());
	}

	public static Vector4f computeBoundingSphere(Iterable<Mesh> meshes) {
		int vertexCount = computeTotalVertexCount(meshes);
		var block = MemoryBlock.malloc((long) vertexCount * PosVertexView.STRIDE);
		var vertexList = new PosVertexView();

		int baseVertex = 0;
		for (Mesh mesh : meshes) {
			vertexList.ptr(block.ptr() + (long) baseVertex * PosVertexView.STRIDE);
			vertexList.vertexCount(mesh.vertexCount());
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
		var center = computeCenterOfAABBContaining(vertexList);

		var radius = computeMaxDistanceTo(vertexList, center) + BOUNDING_SPHERE_EPSILON;

		return new Vector4f(center, radius);
	}

	private static float computeMaxDistanceTo(VertexList vertexList, Vector3f pos) {
		float farthestDistanceSquared = -1;

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			var distanceSquared = pos.distanceSquared(vertexList.x(i), vertexList.y(i), vertexList.z(i));

			if (distanceSquared > farthestDistanceSquared) {
				farthestDistanceSquared = distanceSquared;
			}
		}

		return (float) Math.sqrt(farthestDistanceSquared);
	}

	private static Vector3f computeCenterOfAABBContaining(VertexList vertexList) {
		var min = new Vector3f(Float.MAX_VALUE);
		var max = new Vector3f(Float.MIN_VALUE);

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			float x = vertexList.x(i);
			float y = vertexList.y(i);
			float z = vertexList.z(i);

			// JOML's min/max methods don't accept floats :whywheel:
			min.x = Math.min(min.x, x);
			min.y = Math.min(min.y, y);
			min.z = Math.min(min.z, z);

			max.x = Math.max(max.x, x);
			max.y = Math.max(max.y, y);
			max.z = Math.max(max.z, z);
		}

		return min.add(max)
				.mul(0.5f);
	}
}
