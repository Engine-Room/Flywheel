package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;

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

	public Plan plan(FrameContext ctx) {
		var plans = new ArrayList<Plan>();

		for (var bufferPlan : buffers.values()) {
			plans.add(bufferPlan.update(ctx));
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
		private FrameContext ctx;

		public BufferPlan(DrawBuffer drawBuffer) {
			buffer = drawBuffer;
		}

		public Plan update(FrameContext ctx) {
			this.ctx = ctx;

			// Mark the tracker active by default...
			tracker.markActive(stage, buffer);
			return this;
		}

		public void add(TransformCall<?> transformCall) {
			transformCalls.add(transformCall);
		}

		@Override
		public void execute(TaskExecutor taskExecutor, Runnable onCompletion) {
			// Count vertices here to account for instances being added during Visual updates.
			var vertexCount = setupAndCountVertices();

			if (vertexCount <= 0) {
				// ...then mark it inactive if there's nothing to draw.
				tracker.markInactive(stage, buffer);
				onCompletion.run();
				return;
			}

			AtomicInteger vertexCounter = new AtomicInteger(0);

			buffer.prepare(vertexCount);

			var synchronizer = new Synchronizer(transformCalls.size(), () -> {
				buffer.vertexCount(vertexCounter.get());
				onCompletion.run();
			});

			for (var transformCall : transformCalls) {
				transformCall.plan(ctx, buffer, vertexCounter)
						.execute(taskExecutor, synchronizer::decrementAndEventuallyRun);
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
