package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.model.Mesh;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformSet<D extends InstancedPart> {

	private final CPUInstancer<D> instancer;
	private final Material material;
	private final Mesh mesh;

	public TransformSet(CPUInstancer<D> instancer, Material material, Mesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;
	}

	public Material getMaterial() {
		return material;
	}

	public Mesh getMesh() {
		return mesh;
	}

	void submitTasks(TaskEngine pool, DrawBuffer buffer, int startVertex, PoseStack stack, ClientLevel level) {
		instancer.setup();

		int instances = instancer.getInstanceCount();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int vertexCount = mesh.getVertexCount() * (end - start);
			ReusableVertexList sub = buffer.slice(startVertex, vertexCount);
			startVertex += vertexCount;

			pool.submit(() -> drawRange(sub, start, end, stack, level));
		}
	}

	private void drawRange(ReusableVertexList vertexList, int from, int to, PoseStack stack, ClientLevel level) {
		drawList(vertexList, instancer.getRange(from, to), stack, level);
	}

	void drawAll(ReusableVertexList vertexList, PoseStack stack, ClientLevel level) {
		drawList(vertexList, instancer.getAll(), stack, level);
	}

	private void drawList(ReusableVertexList vertexList, List<D> list, PoseStack stack, ClientLevel level) {
		long anchorPtr = vertexList.ptr();
		int totalVertexCount = vertexList.getVertexCount();

		int meshVertexCount = mesh.getVertexCount();
		vertexList.setVertexCount(meshVertexCount);

		StructType.VertexTransformer<D> structVertexTransformer = instancer.type.getVertexTransformer();

		for (D d : list) {
			mesh.write(vertexList);

			structVertexTransformer.transform(vertexList, d, level);

			vertexList.shiftPtr(meshVertexCount);
		}

		vertexList.ptr(anchorPtr);
		vertexList.setVertexCount(totalVertexCount);
		material.getVertexTransformer().transform(vertexList, level);
		applyPoseStack(vertexList, stack, false);
	}

	private static void applyPoseStack(MutableVertexList vertexList, PoseStack stack, boolean applyNormalMatrix) {
		Vector4f pos = new Vector4f();
		Vector3f normal = new Vector3f();

		Matrix4f modelMatrix = stack.last().pose();
		Matrix3f normalMatrix;
		if (applyNormalMatrix) {
			normalMatrix = stack.last().normal();
		} else {
			normalMatrix = null;
		}

		for (int i = 0; i < vertexList.getVertexCount(); i++) {
			pos.set(
					vertexList.x(i),
					vertexList.y(i),
					vertexList.z(i),
					1f
			);
			pos.transform(modelMatrix);
			vertexList.x(i, pos.x());
			vertexList.y(i, pos.y());
			vertexList.z(i, pos.z());

			if (applyNormalMatrix) {
				normal.set(
						vertexList.normalX(i),
						vertexList.normalY(i),
						vertexList.normalZ(i)
				);
				normal.transform(normalMatrix);
				normal.normalize();
				vertexList.normalX(i, normal.x());
				vertexList.normalY(i, normal.y());
				vertexList.normalZ(i, normal.z());
			}
		}
	}

	public int getTotalVertexCount() {
		return mesh.getVertexCount() * instancer.getInstanceCount();
	}
}
