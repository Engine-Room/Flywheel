package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.extension.RenderTypeExtension;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;

public class BatchedCrumbling {
	public static void render(List<Engine.CrumblingBlock> crumblingBlocks, BatchContext batchContext, BatchedDrawTracker drawTracker) {
		var instancesPerType = doCrumblingSort(crumblingBlocks);

		for (var entry : instancesPerType.entrySet()) {
			// TODO: separate concept of renderstage from drawbuffer
			var drawBuffer = RenderTypeExtension.getDrawBufferSet(entry.getKey())
					.getBuffer(RenderStage.AFTER_BLOCK_ENTITIES);

			var bucket = entry.getValue();

			drawBuffer.prepare(bucket.vertexCount);

			ReusableVertexList vertexList = drawBuffer.slice(0, 0);
			long basePtr = vertexList.ptr();

			int totalVertices = 0;

			for (var pair : bucket.instances) {
				var instance = pair.first();
				var instancer = pair.second();

				totalVertices += bufferOne(instancer, totalVertices, vertexList, drawBuffer, instance);
			}

			vertexList.ptr(basePtr);
			vertexList.vertexCount(totalVertices);

			// apply these in bulk
			BatchingTransforms.applyDecalUVs(vertexList);
			BatchingTransforms.applyMatrices(vertexList, batchContext.matrices());

			drawTracker._draw(drawBuffer);
		}
	}

	@NotNull
	private static Map<RenderType, CrumblingBucket> doCrumblingSort(List<Engine.CrumblingBlock> crumblingBlocks) {
		Map<RenderType, CrumblingBucket> out = new HashMap<>();

		for (Engine.CrumblingBlock crumblingBlock : crumblingBlocks) {
			var crumblingType = ModelBakery.DESTROY_TYPES.get(crumblingBlock.progress());

			for (Instance instance : crumblingBlock.instances()) {
				if (!(instance.handle() instanceof InstanceHandleImpl impl)) {
					continue;
				}
                if (!(impl.instancer instanceof BatchedInstancer<?> instancer)) {
					continue;
                }

				var bucket = out.computeIfAbsent(crumblingType, $ -> new CrumblingBucket());
				bucket.instances.add(Pair.of(instance, instancer));

				for (TransformCall<?> transformCall : instancer.getTransformCalls()) {
					var mesh = transformCall.mesh;
					bucket.vertexCount += mesh.getVertexCount();
				}
            }
		}
		return out;
	}

	private static <I extends Instance> int bufferOne(BatchedInstancer<I> batchedInstancer, int baseVertex, ReusableVertexList vertexList, DrawBuffer drawBuffer, Instance instance) {
		int totalVertices = 0;

		for (TransformCall<I> transformCall : batchedInstancer.getTransformCalls()) {
            Mesh mesh = transformCall.mesh.mesh;

			vertexList.ptr(drawBuffer.ptrForVertex(baseVertex + totalVertices));
			vertexList.vertexCount(mesh.vertexCount());

			mesh.write(vertexList);
			batchedInstancer.type.getVertexTransformer()
					.transform(vertexList, (I) instance);

			totalVertices += mesh.vertexCount();
		}

		return totalVertices;
	}

	private static class CrumblingBucket {
		private int vertexCount;
		private final List<Pair<Instance, BatchedInstancer<?>>> instances = new ArrayList<>();
	}
}
