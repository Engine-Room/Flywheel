package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<D extends InstancedPart> {
	private final CPUInstancer<D> instancer;
	private final Material material;
	private final BatchedMeshPool.BufferedMesh bufferedMesh;

	public TransformCall(CPUInstancer<D> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.bufferedMesh = mesh;
	}

	public Material getMaterial() {
		return material;
	}

	public VertexFormat getVertexFormat() {
		return bufferedMesh.getVertexFormat();
	}

	void submitTasks(TaskEngine pool, DrawBuffer buffer, int startVertex, PoseStack stack, ClientLevel level) {
		instancer.setup();

		int instances = instancer.getInstanceCount();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int vertexCount = bufferedMesh.getMesh().getVertexCount() * (end - start);
			ReusableVertexList sub = buffer.slice(startVertex, vertexCount);
			startVertex += vertexCount;

			pool.submit(() -> transformRange(sub, start, end, stack, level));
		}
	}

	private void transformRange(ReusableVertexList vertexList, int from, int to, PoseStack stack, ClientLevel level) {
		transformList(vertexList, instancer.getRange(from, to), stack, level);
	}

	void transformAll(ReusableVertexList vertexList, PoseStack stack, ClientLevel level) {
		transformList(vertexList, instancer.getAll(), stack, level);
	}

	private void transformList(ReusableVertexList vertexList, List<D> parts, PoseStack stack, ClientLevel level) {
		long anchorPtr = vertexList.ptr();
		int totalVertexCount = vertexList.vertexCount();

		int meshVertexCount = bufferedMesh.getMesh().getVertexCount();
		int meshByteSize = bufferedMesh.size();
		vertexList.vertexCount(meshVertexCount);

		StructType.VertexTransformer<D> structVertexTransformer = instancer.type.getVertexTransformer();

		for (D d : parts) {
			bufferedMesh.copyTo(vertexList.ptr());

			structVertexTransformer.transform(vertexList, d, level);

			vertexList.ptr(vertexList.ptr() + meshByteSize);
		}

		vertexList.ptr(anchorPtr);
		vertexList.vertexCount(totalVertexCount);
		material.getVertexTransformer().transform(vertexList, level);
		applyPoseStack(vertexList, stack);
	}

	private static void applyPoseStack(MutableVertexList vertexList, PoseStack stack) {
		Vector4f pos = new Vector4f();
		Vector3f normal = new Vector3f();

		Matrix4f modelMatrix = stack.last().pose();
		Matrix3f normalMatrix = stack.last().normal();

		for (int i = 0; i < vertexList.vertexCount(); i++) {
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

	public int getTotalVertexCount() {
		return bufferedMesh.getMesh().getVertexCount() * instancer.getInstanceCount();
	}
}
