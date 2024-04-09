package com.jozufozu.flywheel.vanilla.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.vertex.PosTexNormalVertexView;
import com.jozufozu.flywheel.vanilla.mixin.CubeDefinitionAccessor;
import com.jozufozu.flywheel.vanilla.mixin.CubeDeformationAccessor;
import com.jozufozu.flywheel.vanilla.mixin.EntityModelSetAccessor;
import com.jozufozu.flywheel.vanilla.mixin.LayerDefinitionAccessor;
import com.jozufozu.flywheel.vanilla.mixin.MaterialDefinitionAccessor;
import com.jozufozu.flywheel.vanilla.mixin.PartDefinitionAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MeshTree {
	private final PartPose initialPose;
	@Nullable
	private final Mesh mesh;
	private final Map<String, MeshTree> children;

	private MeshTree(PartPose initialPose, @Nullable Mesh mesh, Map<String, MeshTree> children) {
		this.initialPose = initialPose;
		this.mesh = mesh;
		this.children = children;
	}

	public PartPose initialPose() {
		return initialPose;
	}

	@Nullable
	public Mesh mesh() {
		return mesh;
	}

	public Map<String, MeshTree> children() {
		return children;
	}

	@Nullable
	public MeshTree child(String name) {
		return children.get(name);
	}

	public void delete() {
		if (mesh != null) {
			mesh.delete();
		}
		children.values()
				.forEach(MeshTree::delete);
	}

	public static MeshTree convert(ModelLayerLocation location) {
		var entityModels = (EntityModelSetAccessor) Minecraft.getInstance()
				.getEntityModels();

		return convert(entityModels.vanillin$roots()
				.get(location));
	}

	public static MeshTree convert(LayerDefinition layerDefinition) {
		var accessor = (LayerDefinitionAccessor) layerDefinition;
		var root = accessor.vanillin$mesh()
				.getRoot();

		var material = (MaterialDefinitionAccessor) accessor.vanillin$material();

		int xTexSize = material.vanillin$xTexSize();
		int yTexSize = material.vanillin$yTexSize();

		return convert(root, xTexSize, yTexSize);
	}

	private static MeshTree convert(PartDefinition part, int xTexSize, int yTexSize) {
		var accessor = (PartDefinitionAccessor) part;

		var cubes = accessor.vanillin$cubes();
		var initialPose = accessor.vanillin$partPose();
		var childDefinitions = accessor.vanillin$children();

		Map<String, MeshTree> children = new HashMap<>();

		for (Map.Entry<String, PartDefinition> entry : childDefinitions.entrySet()) {
			children.put(entry.getKey(), convert(entry.getValue(), xTexSize, yTexSize));
		}

		return new MeshTree(initialPose, convertCubes(cubes, xTexSize, yTexSize), children);
	}

	private static Mesh convertCubes(List<CubeDefinition> cubes, int xTexSize, int yTexSize) {
		if (cubes.isEmpty()) {
			return null;
		}
		var totalVisibleFaces = countVisibleFaces(cubes);

		if (totalVisibleFaces == 0) {
			return null;
		}

		var totalVertices = totalVisibleFaces * 4;

		var block = MemoryBlock.malloc(totalVertices * PosTexNormalVertexView.STRIDE);
		var view = new PosTexNormalVertexView();

		view.ptr(block.ptr());
		view.vertexCount(totalVertices);

		int vertexIndex = 0;
		for (CubeDefinition cube : cubes) {
			CubeDefinitionAccessor accessor = cast(cube);

			var origin = accessor.vanillin$origin();
			var dimensions = accessor.vanillin$dimensions();
			var grow = (CubeDeformationAccessor) accessor.vanillin$grow();
			var pMirror = accessor.vanillin$mirror();
			var texCoord = accessor.vanillin$texCoord();
			var texScale = accessor.vanillin$texScale();

			var visibleFaces = accessor.vanillin$visibleFaces();

			float pOriginX = origin.x();
			float pOriginY = origin.y();
			float pOriginZ = origin.z();

			float pDimensionX = dimensions.x();
			float pDimensionY = dimensions.y();
			float pDimensionZ = dimensions.z();

			float pGrowX = grow.vanillin$growX();
			float pGrowY = grow.vanillin$growY();
			float pGrowZ = grow.vanillin$growZ();

			float pTexCoordU = texCoord.u();
			float pTexCoordV = texCoord.v();

			float pTexScaleU = xTexSize * texScale.u();
			float pTexScaleV = yTexSize * texScale.v();

			float f = pOriginX + pDimensionX;
			float f1 = pOriginY + pDimensionY;
			float f2 = pOriginZ + pDimensionZ;
			pOriginX -= pGrowX;
			pOriginY -= pGrowY;
			pOriginZ -= pGrowZ;
			f += pGrowX;
			f1 += pGrowY;
			f2 += pGrowZ;
			if (pMirror) {
				float f3 = f;
				f = pOriginX;
				pOriginX = f3;
			}

			Vertex modelpart$vertex7 = new Vertex(pOriginX, pOriginY, pOriginZ, 0.0F, 0.0F);
			Vertex modelpart$vertex = new Vertex(f, pOriginY, pOriginZ, 0.0F, 8.0F);
			Vertex modelpart$vertex1 = new Vertex(f, f1, pOriginZ, 8.0F, 8.0F);
			Vertex modelpart$vertex2 = new Vertex(pOriginX, f1, pOriginZ, 8.0F, 0.0F);
			Vertex modelpart$vertex3 = new Vertex(pOriginX, pOriginY, f2, 0.0F, 0.0F);
			Vertex modelpart$vertex4 = new Vertex(f, pOriginY, f2, 0.0F, 8.0F);
			Vertex modelpart$vertex5 = new Vertex(f, f1, f2, 8.0F, 8.0F);
			Vertex modelpart$vertex6 = new Vertex(pOriginX, f1, f2, 8.0F, 0.0F);
			float f4 = pTexCoordU;
			float f5 = pTexCoordU + pDimensionZ;
			float f6 = pTexCoordU + pDimensionZ + pDimensionX;
			float f7 = pTexCoordU + pDimensionZ + pDimensionX + pDimensionX;
			float f8 = pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ;
			float f9 = pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ + pDimensionX;
			float f10 = pTexCoordV;
			float f11 = pTexCoordV + pDimensionZ;
			float f12 = pTexCoordV + pDimensionZ + pDimensionY;
			if (visibleFaces.contains(Direction.DOWN)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex4, modelpart$vertex3, modelpart$vertex7, modelpart$vertex}, f5, f10, f6, f11, pTexScaleU, pTexScaleV, pMirror, Direction.DOWN);
				vertexIndex += 4;
			}

			if (visibleFaces.contains(Direction.UP)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex1, modelpart$vertex2, modelpart$vertex6, modelpart$vertex5}, f6, f11, f7, f10, pTexScaleU, pTexScaleV, pMirror, Direction.UP);
				vertexIndex += 4;
			}

			if (visibleFaces.contains(Direction.WEST)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex7, modelpart$vertex3, modelpart$vertex6, modelpart$vertex2}, f4, f11, f5, f12, pTexScaleU, pTexScaleV, pMirror, Direction.WEST);
				vertexIndex += 4;
			}

			if (visibleFaces.contains(Direction.NORTH)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex, modelpart$vertex7, modelpart$vertex2, modelpart$vertex1}, f5, f11, f6, f12, pTexScaleU, pTexScaleV, pMirror, Direction.NORTH);
				vertexIndex += 4;
			}

			if (visibleFaces.contains(Direction.EAST)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex4, modelpart$vertex, modelpart$vertex1, modelpart$vertex5}, f6, f11, f8, f12, pTexScaleU, pTexScaleV, pMirror, Direction.EAST);
				vertexIndex += 4;
			}

			if (visibleFaces.contains(Direction.SOUTH)) {
				addFace(view, vertexIndex, new Vertex[]{modelpart$vertex3, modelpart$vertex4, modelpart$vertex5, modelpart$vertex6}, f8, f11, f9, f12, pTexScaleU, pTexScaleV, pMirror, Direction.SOUTH);
				vertexIndex += 4;
			}
		}

		return new SimpleMesh(view, block);
	}

	private static void addFace(PosTexNormalVertexView view, int index, Vertex[] pVertices, float pU1, float pV1, float pU2, float pV2, float pTextureWidth, float pTextureHeight, boolean pMirror, Direction pDirection) {
		float f = 0.0F / pTextureWidth;
		float f1 = 0.0F / pTextureHeight;
		pVertices[0] = pVertices[0].remap(pU2 / pTextureWidth - f, pV1 / pTextureHeight + f1);
		pVertices[1] = pVertices[1].remap(pU1 / pTextureWidth + f, pV1 / pTextureHeight + f1);
		pVertices[2] = pVertices[2].remap(pU1 / pTextureWidth + f, pV2 / pTextureHeight - f1);
		pVertices[3] = pVertices[3].remap(pU2 / pTextureWidth - f, pV2 / pTextureHeight - f1);
		if (pMirror) {
			int i = pVertices.length;

			for (int j = 0; j < i / 2; ++j) {
				Vertex modelpart$vertex = pVertices[j];
				pVertices[j] = pVertices[i - 1 - j];
				pVertices[i - 1 - j] = modelpart$vertex;
			}
		}

		var normal = pDirection.step();
		if (pMirror) {
			normal.mul(-1.0F, 1.0F, 1.0F);
		}

		int i = index;
		for (Vertex modelpart$vertex : pVertices) {
			float f3 = modelpart$vertex.x() / 16.0F;
			float f4 = modelpart$vertex.y() / 16.0F;
			float f5 = modelpart$vertex.z() / 16.0F;
			view.x(i, f3);
			view.y(i, f4);
			view.z(i, f5);
			view.u(i, modelpart$vertex.u());
			view.v(i, modelpart$vertex.v());
			view.normalX(i, normal.x());
			view.normalY(i, normal.y());
			view.normalZ(i, normal.z());

			++i;
		}
	}

	private static int countVisibleFaces(List<CubeDefinition> cubes) {
		int totalVisibleFaces = 0;
		for (CubeDefinition cube : cubes) {
			totalVisibleFaces += cast(cube).vanillin$visibleFaces()
					.size();
		}
		return totalVisibleFaces;
	}

	@NotNull
	private static CubeDefinitionAccessor cast(CubeDefinition cube) {
		return (CubeDefinitionAccessor) (Object) cube;
	}

	@OnlyIn(Dist.CLIENT)
	record Vertex(float x, float y, float z, float u, float v) {
		public Vertex remap(float pU, float pV) {
			return new Vertex(x, y, z, pU, pV);
		}
	}
}
