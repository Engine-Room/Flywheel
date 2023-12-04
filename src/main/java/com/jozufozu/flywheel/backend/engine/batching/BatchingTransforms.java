package com.jozufozu.flywheel.backend.engine.batching;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;

import net.minecraft.core.Direction;

public class BatchingTransforms {
	public static void applyMatrices(MutableVertexList vertexList, PoseStack.Pose matrices) {
		Matrix4f modelMatrix = matrices.pose();
		Matrix3f normalMatrix = matrices.normal();

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			VertexTransformations.transformPos(vertexList, i, modelMatrix);
			VertexTransformations.transformNormal(vertexList, i, normalMatrix);
		}
	}

	/**
	 * Performs the same operation as {@link SheetedDecalTextureGenerator} in-place.
	 * <br>
	 * Call this in world space.
	 *
	 * @param vertexList The vertex list to apply the transformations to.
	 */
	public static void applyDecalUVs(MutableVertexList vertexList) {
		Vector3f normal = new Vector3f();
		Vector4f pos = new Vector4f();

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			vertexList.getPos(i, pos);
			vertexList.getNormal(i, normal);

			Direction direction = Direction.getNearest(normal.x(), normal.y(), normal.z());
			pos.rotateY((float)Math.PI);
			pos.rotateX((-(float)Math.PI / 2F));
			pos.rotate(direction.getRotation());
			float u = -pos.x();
			float v = -pos.y();

			vertexList.u(i, u);
			vertexList.v(i, v);
		}
	}
}
