package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;

import net.minecraft.client.renderer.RenderType;

/**
 * All the rendering that happens within a render stage.
 */
public class BatchingStage implements SimplyComposedPlan<BatchContext> {
	private final RenderStage stage;
	private final BatchingDrawTracker tracker;
	private final Map<RenderType, BufferPlan> buffers = new HashMap<>();

	public BatchingStage(RenderStage renderStage, BatchingDrawTracker tracker) {
		stage = renderStage;
		this.tracker = tracker;
	}

	@Override
	public void execute(TaskExecutor taskExecutor, BatchContext context, Runnable onCompletion) {
		if (buffers.isEmpty()) {
			onCompletion.run();
			return;
		}

		taskExecutor.execute(() -> {
			var sync = new Synchronizer(buffers.size(), onCompletion);

			for (var buffer : buffers.values()) {
				buffer.execute(taskExecutor, context, sync);
			}
		});
	}

	public void put(RenderType renderType, TransformCall<?> transformCall) {
		buffers.computeIfAbsent(renderType, type -> new BufferPlan(BatchingDrawTracker.getBuffer(type, stage)))
				.add(transformCall);
	}

	public boolean isEmpty() {
		return buffers.isEmpty();
	}

	private class BufferPlan implements SimplyComposedPlan<BatchContext> {
		private final DrawBuffer buffer;
		private final List<TransformCall<?>> transformCalls = new ArrayList<>();

		public BufferPlan(DrawBuffer drawBuffer) {
			buffer = drawBuffer;
		}

		public void add(TransformCall<?> transformCall) {
			transformCalls.add(transformCall);
		}

		@Override
		public void execute(TaskExecutor taskExecutor, BatchContext ctx, Runnable onCompletion) {
			var vertexCount = setupAndCountVertices();

			if (vertexCount <= 0) {
				onCompletion.run();
				return;
			}

			tracker.markActive(stage, buffer);

			var vertexCounter = new AtomicInteger(0);

			buffer.prepare(vertexCount);

			var synchronizer = new Synchronizer(transformCalls.size(), () -> {
				buffer.vertexCount(vertexCounter.get());
				onCompletion.run();
			});

			var planContext = new TransformCall.PlanContext(ctx, buffer, vertexCounter);

			for (var transformCall : transformCalls) {
				transformCall.plan()
						.execute(taskExecutor, planContext, synchronizer);
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
