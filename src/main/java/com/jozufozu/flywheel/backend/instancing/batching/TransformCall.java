package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.backend.instancing.TaskExecutor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<D extends InstancedPart> {
	private final CPUInstancer<D> instancer;
	private final Material material;
	private final BatchedMeshPool.BufferedMesh mesh;

	private final int meshVertexCount;
	private final int meshByteSize;

	public TransformCall(CPUInstancer<D> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;

		meshVertexCount = mesh.getVertexCount();
		meshByteSize = mesh.size();
	}

	public int getTotalVertexCount() {
		return meshVertexCount * instancer.getInstanceCount();
	}

	void setup() {
		instancer.update();
	}

	void submitTasks(TaskExecutor executor, DrawBuffer buffer, int startVertex, PoseStack.Pose matrices, ClientLevel level) {
		int instances = instancer.getInstanceCount();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int vertexCount = meshVertexCount * (end - start);
			ReusableVertexList sub = buffer.slice(startVertex, vertexCount);
			startVertex += vertexCount;

			executor.execute(() -> transformRange(sub, start, end, matrices, level));
		}
	}

	private void transformRange(ReusableVertexList vertexList, int from, int to, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getRange(from, to), matrices, level);
	}

	void transformAll(ReusableVertexList vertexList, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getAll(), matrices, level);
	}

	private void transformList(ReusableVertexList vertexList, List<D> parts, PoseStack.Pose matrices, ClientLevel level) {
		long anchorPtr = vertexList.ptr();
		int totalVertexCount = vertexList.vertexCount();

		vertexList.vertexCount(meshVertexCount);

		StructType.VertexTransformer<D> structVertexTransformer = instancer.type.getVertexTransformer();

		for (D d : parts) {
			mesh.copyTo(vertexList.ptr());

			structVertexTransformer.transform(vertexList, d, level);

			vertexList.ptr(vertexList.ptr() + meshByteSize);
		}

		vertexList.ptr(anchorPtr);
		vertexList.vertexCount(totalVertexCount);
		material.getVertexTransformer().transform(vertexList, level);
		applyMatrices(vertexList, matrices);
	}

	private static void applyMatrices(MutableVertexList vertexList, PoseStack.Pose matrices) {
		Vector4f pos = new Vector4f();
		Vector3f normal = new Vector3f();

		Matrix4f modelMatrix = matrices.pose();
		Matrix3f normalMatrix = matrices.normal();

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
}
