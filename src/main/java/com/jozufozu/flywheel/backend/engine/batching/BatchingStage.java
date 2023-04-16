package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;
import com.jozufozu.flywheel.lib.task.UnitPlan;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;

/**
 * All the rendering that happens within a render stage.
 */
public class BatchingStage {
	private final RenderStage stage;
	private final BatchingDrawTracker tracker;
	private final Map<RenderType, BufferPlan> buffers = new HashMap<>();

	public BatchingStage(RenderStage renderStage, BatchingDrawTracker tracker) {
		stage = renderStage;
		this.tracker = tracker;
	}

	public Plan plan(PoseStack.Pose matrices, ClientLevel level) {
		var plans = new ArrayList<Plan>();

		for (var bufferPlan : buffers.values()) {
			plans.add(bufferPlan.update(matrices, level));
		}

		return new NestedPlan(plans);
	}

	public void put(RenderType renderType, TransformCall<?> transformCall) {
		buffers.computeIfAbsent(renderType, type -> new BufferPlan(BatchingDrawTracker.getBuffer(type, stage)))
				.add(transformCall);
	}

	public boolean isEmpty() {
		return buffers.isEmpty();
	}

	private class BufferPlan implements Plan {
		private final DrawBuffer buffer;
		private final List<TransformCall<?>> transformCalls = new ArrayList<>();
		private PoseStack.Pose matrices;
		private ClientLevel level;
		private int vertexCount;

		public BufferPlan(DrawBuffer drawBuffer) {
			buffer = drawBuffer;
		}

		public Plan update(PoseStack.Pose matrices, ClientLevel level) {
			this.matrices = matrices;
			this.level = level;

			vertexCount = setupAndCountVertices();
			if (vertexCount <= 0) {
				return UnitPlan.INSTANCE;
			}

			// Moving this into execute leads to a race condition that causes things to flash in and out of existence.
			// Sometimes the main thread decides there's nothing to render in a stage before the worker threads have
			// marked a stage as active. Then in the next frame #markActive complains because it's already prepared.
			tracker.markActive(stage, buffer);
			return this;
		}

		public void add(TransformCall<?> transformCall) {
			transformCalls.add(transformCall);
		}

		@Override
		public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
			buffer.prepare(vertexCount);

			var synchronizer = new Synchronizer(transformCalls.size(), onCompletion);

			int startVertex = 0;
			for (var transformCall : transformCalls) {
				transformCall.plan(buffer, startVertex, matrices, level)
						.execute(taskExecutor, synchronizer::decrementAndEventuallyRun);
				startVertex += transformCall.getTotalVertexCount();
			}
		}

		private int setupAndCountVertices() {
			int vertices = 0;
			for (var transformCall : transformCalls) {
				transformCall.setup();
				vertices += transformCall.getTotalVertexCount();
			}
			return vertices;
		}
	}
}
