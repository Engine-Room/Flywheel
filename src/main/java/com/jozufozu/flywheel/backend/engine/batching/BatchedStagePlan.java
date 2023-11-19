package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Flag;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.StageFlag;
import com.jozufozu.flywheel.lib.task.Synchronizer;

import net.minecraft.client.renderer.RenderType;

/**
 * All the rendering that happens within a render stage.
 */
public class BatchedStagePlan implements SimplyComposedPlan<BatchContext> {
	/**
	 * This flag will be raised when this stage completes execution.
	 */
	public final Flag flag;

	private final RenderStage stage;
	private final BatchedDrawTracker tracker;
	private final Map<RenderType, BufferPlan> bufferPlans = new HashMap<>();

	public BatchedStagePlan(RenderStage stage, BatchedDrawTracker tracker) {
		this.flag = new StageFlag(stage);
		this.stage = stage;
		this.tracker = tracker;
	}

	@Override
	public void execute(TaskExecutor taskExecutor, BatchContext context, Runnable onCompletion) {
		if (isEmpty()) {
			taskExecutor.raise(flag);
			onCompletion.run();
			return;
		}

		taskExecutor.execute(() -> {
			var sync = new Synchronizer(bufferPlans.size(), () -> {
				taskExecutor.raise(flag);
				onCompletion.run();
			});

			for (var plan : bufferPlans.values()) {
				plan.execute(taskExecutor, context, sync);
			}
		});
	}

	public void put(RenderType renderType, TransformCall<?> transformCall) {
		bufferPlans.computeIfAbsent(renderType, type -> new BufferPlan(DrawBuffer.get(type, stage)))
				.add(transformCall);
	}

	public boolean isEmpty() {
		return bufferPlans.isEmpty();
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

			tracker.markActive(buffer);
			buffer.prepare(vertexCount);

			var vertexCounter = new AtomicInteger(0);
			var planContext = new TransformCall.PlanContext(ctx.frustum(), vertexCounter, buffer, ctx.level(), ctx.matrices());

			var synchronizer = new Synchronizer(transformCalls.size(), () -> {
				buffer.verticesToDraw(vertexCounter.get());
				onCompletion.run();
			});

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
