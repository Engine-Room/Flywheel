package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;

public class TransformSet {
	private final RenderStage stage;
	private final ListMultimap<RenderType, TransformCall<?>> transformCalls;

	public TransformSet(RenderStage renderStage) {
		stage = renderStage;
		transformCalls = ArrayListMultimap.create();
	}

	public Plan plan(PoseStack.Pose matrices, ClientLevel level, BatchingDrawTracker tracker) {
		var plans = new ArrayList<Plan>();

		for (var entry : transformCalls.asMap()
				.entrySet()) {
			var renderType = entry.getKey();
			var transformCalls = entry.getValue();

			int vertices = 0;
			for (var transformCall : transformCalls) {
				transformCall.setup();
				vertices += transformCall.getTotalVertexCount();
			}

			if (vertices == 0) {
				continue;
			}

			DrawBuffer buffer = tracker.getBuffer(renderType, this.stage);
			buffer.prepare(vertices);

			int startVertex = 0;
			for (var transformCall : transformCalls) {
				plans.add(transformCall.plan(buffer, startVertex, matrices, level));
				startVertex += transformCall.getTotalVertexCount();
			}
		}

		return new NestedPlan(plans);
	}

	public void put(RenderType shaderState, TransformCall<?> transformCall) {
		transformCalls.put(shaderState, transformCall);
	}

	public boolean isEmpty() {
		return transformCalls.isEmpty();
	}
}
