package com.jozufozu.flywheel.backend.engine.batching;

import java.util.concurrent.atomic.AtomicInteger;

import org.joml.FrustumIntersection;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceBoundingSphereTransformer;
import com.jozufozu.flywheel.api.instance.InstanceVertexTransformer;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.MaterialVertexTransformer;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.lib.task.ForEachPlan;
import com.jozufozu.flywheel.lib.vertex.VertexTransformations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<I extends Instance> {
	private final BatchedInstancer<I> instancer;
	private final int meshVertexCount;

	private final Plan<PlanContext> drawPlan;

	public TransformCall(BatchedInstancer<I> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;

		InstanceVertexTransformer<I> instanceVertexTransformer = instancer.type.getVertexTransformer();
		InstanceBoundingSphereTransformer<I> boundingSphereTransformer = instancer.type.getBoundingSphereTransformer();
		MaterialVertexTransformer materialVertexTransformer = material.getVertexTransformer();

		meshVertexCount = mesh.getVertexCount();
		Vector4fc meshBoundingSphere = mesh.getBoundingSphere();

		drawPlan = ForEachPlan.of(instancer::getAll, (instance, ctx) -> {
			var boundingSphere = new Vector4f(meshBoundingSphere);
			boundingSphereTransformer.transform(boundingSphere, instance);

			if (!ctx.frustum
					.testSphere(boundingSphere.x, boundingSphere.y, boundingSphere.z, boundingSphere.w)) {
				return;
			}

			final int baseVertex = ctx.vertexCounter.getAndAdd(meshVertexCount);
			var sub = ctx.buffer.slice(baseVertex, meshVertexCount);

			mesh.copyTo(sub.ptr());
			instanceVertexTransformer.transform(sub, instance);
			materialVertexTransformer.transform(sub, ctx.level);
			applyMatrices(sub, ctx.matrices);
		});
	}

	public void setup() {
		instancer.update();
	}

	public int getTotalVertexCount() {
		return meshVertexCount * instancer.getInstanceCount();
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

	public record PlanContext(FrustumIntersection frustum, AtomicInteger vertexCounter, DrawBuffer buffer, ClientLevel level, PoseStack.Pose matrices) {
	}
}
