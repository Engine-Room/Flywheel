package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.InstancePart;
import com.jozufozu.flywheel.api.struct.StructVertexTransformer;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<P extends InstancePart> {
	private final CPUInstancer<P> instancer;
	private final Material material;
	private final BatchedMeshPool.BufferedMesh mesh;

	private final int meshVertexCount;
	private final int meshByteSize;

	public TransformCall(CPUInstancer<P> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;

		meshVertexCount = mesh.getVertexCount();
		meshByteSize = mesh.size();
	}

	public int getTotalVertexCount() {
		return meshVertexCount * instancer.getInstanceCount();
	}

	public void setup() {
		instancer.update();
	}

	public void submitTasks(TaskExecutor executor, DrawBuffer buffer, int startVertex, PoseStack.Pose matrices, ClientLevel level) {
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

	public void transformRange(ReusableVertexList vertexList, int from, int to, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getRange(from, to), matrices, level);
	}

	public void transformAll(ReusableVertexList vertexList, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getAll(), matrices, level);
	}

	public void transformList(ReusableVertexList vertexList, List<P> parts, PoseStack.Pose matrices, ClientLevel level) {
		long anchorPtr = vertexList.ptr();
		int totalVertexCount = vertexList.vertexCount();

		vertexList.vertexCount(meshVertexCount);

		StructVertexTransformer<P> structVertexTransformer = instancer.type.getVertexTransformer();

		for (P p : parts) {
			mesh.copyTo(vertexList.ptr());

			structVertexTransformer.transform(vertexList, p, level);

			vertexList.ptr(vertexList.ptr() + meshByteSize);
		}

		vertexList.ptr(anchorPtr);
		vertexList.vertexCount(totalVertexCount);
		material.getVertexTransformer().transform(vertexList, level);
		applyMatrices(vertexList, matrices);
	}

	private static void applyMatrices(MutableVertexList vertexList, PoseStack.Pose matrices) {
		Matrix4f modelMatrix = matrices.pose();
		Matrix3f normalMatrix = matrices.normal();

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			VertexTransformations.transformPos(vertexList, i, modelMatrix);
			VertexTransformations.transformNormal(vertexList, i, normalMatrix);
		}
	}
}
