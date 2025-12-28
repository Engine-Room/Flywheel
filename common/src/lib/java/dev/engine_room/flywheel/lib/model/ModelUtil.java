package dev.engine_room.flywheel.lib.model;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.VertexList;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.vertex.PosVertexView;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ModelUtil {
	private static final float BOUNDING_SPHERE_EPSILON = 1e-4f;

	private static final ChunkSectionLayer[] CHUNK_LAYERS = new ChunkSectionLayer[]{ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT, ChunkSectionLayer.TRANSLUCENT, ChunkSectionLayer.TRIPWIRE};

	// Array of chunk materials to make lookups easier.
	// Index by (renderTypeIdx * 4 + shaded * 2 + ambientOcclusion).
	private static final Material[] CHUNK_MATERIALS = new Material[20];

	static {
		Material[] baseChunkMaterials = new Material[]{Materials.SOLID_BLOCK, Materials.CUTOUT_BLOCK, Materials.TRANSLUCENT_BLOCK, Materials.TRIPWIRE_BLOCK};
		for (int chunkLayerIdx = 0; chunkLayerIdx < CHUNK_LAYERS.length; chunkLayerIdx++) {
			int baseMaterialIdx = chunkLayerIdx * 4;
			Material baseChunkMaterial = baseChunkMaterials[chunkLayerIdx];

			// shaded: false, ambientOcclusion: false
			CHUNK_MATERIALS[baseMaterialIdx] = SimpleMaterial.builderOf(baseChunkMaterial)
					.cardinalLightingMode(CardinalLightingMode.OFF)
					.ambientOcclusion(false)
					.build();
			// shaded: false, ambientOcclusion: true
			CHUNK_MATERIALS[baseMaterialIdx + 1] = SimpleMaterial.builderOf(baseChunkMaterial)
					.cardinalLightingMode(CardinalLightingMode.OFF)
					.build();
			// shaded: true, ambientOcclusion: false
			CHUNK_MATERIALS[baseMaterialIdx + 2] = SimpleMaterial.builderOf(baseChunkMaterial)
					.ambientOcclusion(false)
					.build();
			// shaded: true, ambientOcclusion: true
			CHUNK_MATERIALS[baseMaterialIdx + 3] = baseChunkMaterial;
		}
	}

	private ModelUtil() {
	}

	@Nullable
	public static Material getMaterial(ChunkSectionLayer chunkRenderType, boolean shaded) {
		return getMaterial(chunkRenderType, shaded, true);
	}

	@Nullable
	public static Material getMaterial(ChunkSectionLayer chunkRenderType, boolean shaded, boolean ambientOcclusion) {
		for (int chunkLayerIdx = 0; chunkLayerIdx < CHUNK_LAYERS.length; ++chunkLayerIdx) {
			if (chunkRenderType == CHUNK_LAYERS[chunkLayerIdx]) {
				int shadedIdx = shaded ? 1 : 0;
				int ambientOcclusionIdx = ambientOcclusion ? 1 : 0;

				int materialIdx = chunkLayerIdx * 4 + shadedIdx * 2 + ambientOcclusionIdx;

				return CHUNK_MATERIALS[materialIdx];
			}
		}
		return null;
	}

	@Nullable
	public static Material getItemMaterial(RenderType renderType) {
		if (renderType == Sheets.cutoutBlockSheet()) {
			return Materials.CUTOUT_BLOCK;
		}

		if (renderType == Sheets.solidBlockSheet()) {
			return Materials.SOLID_BLOCK;
		}

		if (renderType == Sheets.translucentBlockItemSheet() || renderType == Sheets.translucentItemSheet()) {
			return Materials.TRANSLUCENT_ENTITY;
		}
		if (renderType == RenderTypes.glint()) {
			return Materials.GLINT;
		}
		if (renderType == RenderTypes.glintTranslucent()) {
			return Materials.TRANSLUCENT_GLINT;
		}
		if (renderType == RenderTypes.entityGlint() || renderType == RenderTypes.armorEntityGlint()) {
			return Materials.GLINT_ENTITY;
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
