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
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.task.ForEachSlicePlan;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;

public class TransformCall<I extends Instance> {
	public final BatchedInstancer<I> instancer;
	public final Material material;
	public final BatchedMeshPool.BufferedMesh mesh;

	private final int meshVertexCount;
	private final Plan<PlanContext> drawPlan;

	public TransformCall(BatchedInstancer<I> instancer, Material material, BatchedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;

		InstanceVertexTransformer<I> instanceVertexTransformer = instancer.type.getVertexTransformer();
		InstanceBoundingSphereTransformer<I> boundingSphereTransformer = instancer.type.getBoundingSphereTransformer();
		MaterialVertexTransformer materialVertexTransformer = material.getVertexTransformer();

		meshVertexCount = mesh.vertexCount();
		Vector4fc meshBoundingSphere = mesh.boundingSphere();

		drawPlan = ForEachSlicePlan.of(instancer::getAll, (subList, ctx) -> {
			VertexView vertexView = ctx.buffer.slice(0, meshVertexCount);
			Vector4f boundingSphere = new Vector4f();

			for (I instance : subList) {
				boundingSphere.set(meshBoundingSphere);
				boundingSphereTransformer.transform(boundingSphere, instance);

				if (!ctx.frustum.testSphere(boundingSphere.x, boundingSphere.y, boundingSphere.z, boundingSphere.w)) {
					continue;
				}

				final int baseVertex = ctx.vertexCounter.getAndAdd(meshVertexCount);
				vertexView.ptr(ctx.buffer.ptrForVertex(baseVertex));

				mesh.copyTo(vertexView.ptr());
				instanceVertexTransformer.transform(vertexView, instance);
				materialVertexTransformer.transform(vertexView, ctx.level);
				BatchingTransforms.applyMatrices(vertexView, ctx.matrices);
			}
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

	public record PlanContext(FrustumIntersection frustum, AtomicInteger vertexCounter, DrawBuffer buffer, ClientLevel level, PoseStack.Pose matrices) {
	}
}
