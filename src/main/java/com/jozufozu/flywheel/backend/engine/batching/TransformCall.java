package com.jozufozu.flywheel.backend.engine.batching;

import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceBoundingSphereTransformer;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.task.RunOnAllWithContextPlan;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public class TransformCall<I extends Instance> {
	private final CPUInstancer<I> instancer;
	private final Material material;
	private final BatchedMeshPool.BufferedMesh mesh;

	private final int meshVertexCount;
	private final int meshByteSize;
	private final InstanceVertexTransformer<I> instanceVertexTransformer;
	private final MaterialVertexTransformer materialVertexTransformer;
	private final InstanceBoundingSphereTransformer<I> boundingSphereTransformer;
	private final Vector4fc boundingSphere;

	private final Plan<PlanContext> drawPlan;

	public TransformCall(CPUInstancer<I> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;

		instanceVertexTransformer = instancer.type.getVertexTransformer();
		boundingSphereTransformer = instancer.type.getBoundingSphereTransformer();
		materialVertexTransformer = material.getVertexTransformer();

		meshVertexCount = mesh.getVertexCount();
		meshByteSize = mesh.size();
		boundingSphere = mesh.mesh.getBoundingSphere();

		drawPlan = RunOnAllWithContextPlan.of(instancer::getAll, (instance, ctx) -> {
			var boundingSphere = new Vector4f(this.boundingSphere);

			boundingSphereTransformer.transform(boundingSphere, instance);

			if (!ctx.ctx.frustum()
					.testSphere(boundingSphere.x, boundingSphere.y, boundingSphere.z, boundingSphere.w)) {
				return;
			}

			final int baseVertex = ctx.vertexCount.getAndAdd(meshVertexCount);

			if (baseVertex + meshVertexCount > ctx.buffer.getVertexCount()) {
				throw new IndexOutOfBoundsException("Vertex count greater than allocated: " + baseVertex + " + " + meshVertexCount + " > " + ctx.buffer.getVertexCount());
			}

			var sub = ctx.buffer.slice(baseVertex, meshVertexCount);

			mesh.copyTo(sub.ptr());

			instanceVertexTransformer.transform(sub, instance, ctx.ctx.level());

			materialVertexTransformer.transform(sub, ctx.ctx.level());
			applyMatrices(sub, ctx.ctx.matrices());
		});
	}

	public int getTotalVertexCount() {
		return meshVertexCount * instancer.getInstanceCount();
	}

	public void setup() {
		instancer.update();
	}

	public Plan<PlanContext> plan() {
		return drawPlan;
	}

	private static void applyMatrices(MutableVertexList vertexList, PoseStack.Pose matrices) {
		Matrix4f modelMatrix = matrices.pose();
		Matrix3f normalMatrix = matrices.normal();

		for (int i = 0; i < vertexList.vertexCount(); i++) {
			VertexTransformations.transformPos(vertexList, i, modelMatrix);
			VertexTransformations.transformNormal(vertexList, i, normalMatrix);
		}
	}

	public record PlanContext(BatchContext ctx, DrawBuffer buffer, AtomicInteger vertexCount) {
	}
}
