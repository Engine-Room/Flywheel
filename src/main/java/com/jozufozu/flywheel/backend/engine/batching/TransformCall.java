package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.lib.task.SimplePlan;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<I extends Instance> {
	private final CPUInstancer<I> instancer;
	private final Material material;
	private final BatchedMeshPool.BufferedMesh mesh;

	private final int meshVertexCount;
	private final int meshByteSize;

	public TransformCall(CPUInstancer<I> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
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

	public Plan getPlan(DrawBuffer buffer, int startVertex, PoseStack.Pose matrices, ClientLevel level) {
		int instances = instancer.getInstanceCount();

		var out = new ArrayList<Runnable>();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int vertexCount = meshVertexCount * (end - start);
			ReusableVertexList sub = buffer.slice(startVertex, vertexCount);
			startVertex += vertexCount;

			out.add(() -> transformRange(sub, start, end, matrices, level));
		}
		return new SimplePlan(out);
	}

	public void transformRange(ReusableVertexList vertexList, int from, int to, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getRange(from, to), matrices, level);
	}

	public void transformAll(ReusableVertexList vertexList, PoseStack.Pose matrices, ClientLevel level) {
		transformList(vertexList, instancer.getAll(), matrices, level);
	}

	public void transformList(ReusableVertexList vertexList, List<I> instances, PoseStack.Pose matrices, ClientLevel level) {
		long anchorPtr = vertexList.ptr();
		int totalVertexCount = vertexList.vertexCount();

		vertexList.vertexCount(meshVertexCount);

		InstanceVertexTransformer<I> instanceVertexTransformer = instancer.type.getVertexTransformer();

		for (I instance : instances) {
			mesh.copyTo(vertexList.ptr());

			instanceVertexTransformer.transform(vertexList, instance, level);

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
