package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.lib.math.MoreMath;
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

	public Plan plan(DrawBuffer buffer, int startVertex, PoseStack.Pose matrices, ClientLevel level) {
		final int totalCount = instancer.getInstanceCount();
		final int chunkSize = MoreMath.ceilingDiv(totalCount, 6 * 32);

		final var out = new ArrayList<Runnable>();
		int remaining = totalCount;
		while (remaining > 0) {
			int end = remaining;
			remaining -= chunkSize;
			int start = Math.max(remaining, 0);

			int vertexCount = meshVertexCount * (end - start);
			ReusableVertexList sub = buffer.slice(startVertex, vertexCount);
			startVertex += vertexCount;

			out.add(() -> transform(sub, matrices, level, instancer.getRange(start, end)));
		}
		return new SimplePlan(out);
	}

	private void transform(ReusableVertexList vertexList, PoseStack.Pose matrices, ClientLevel level, List<I> instances) {
		// save the total size of the slice for later.
		final long anchorPtr = vertexList.ptr();
		final int totalVertexCount = vertexList.vertexCount();

		// while working on individual instances, the vertex list should expose just a single copy of the mesh.
		vertexList.vertexCount(meshVertexCount);

		InstanceVertexTransformer<I> instanceVertexTransformer = instancer.type.getVertexTransformer();

		for (I instance : instances) {
			mesh.copyTo(vertexList.ptr());

			instanceVertexTransformer.transform(vertexList, instance, level);

			vertexList.ptr(vertexList.ptr() + meshByteSize);
		}

		// restore the original size of the slice to apply per-vertex transformations.
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
