package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public class BatchingEngine extends AbstractEngine {
	private final BatchingTransformManager transformManager = new BatchingTransformManager();
	private final BatchingDrawTracker drawTracker = new BatchingDrawTracker();

	public BatchingEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage) {
		return transformManager.getInstancer(type, model, stage);
	}

	@Override
	public void beginFrame(TaskExecutor executor, RenderContext context) {
		transformManager.flush();

		var stack = FlwUtil.copyPoseStack(context.stack());
		Vec3 cameraPos = context.camera().getPosition();
		stack.translate(renderOrigin.getX() - cameraPos.x, renderOrigin.getY() - cameraPos.y, renderOrigin.getZ() - cameraPos.z);

		// TODO: async task executor barriers
		executor.syncPoint();
		submitTasks(executor, stack.last(), context.level());
	}

	private void submitTasks(TaskExecutor executor, PoseStack.Pose matrices, ClientLevel level) {
		for (var transformSetEntry : transformManager.getTransformSetsView().entrySet()) {
			var stage = transformSetEntry.getKey();
			var transformSet = transformSetEntry.getValue();

			for (var entry : transformSet) {
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

				DrawBuffer buffer = drawTracker.getBuffer(renderType, stage);
				buffer.prepare(vertices);

				int startVertex = 0;
				for (var transformCall : transformCalls) {
					transformCall.submitTasks(executor, buffer, startVertex, matrices, level);
					startVertex += transformCall.getTotalVertexCount();
				}
			}
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		drawTracker.draw(stage);
	}

	@Override
	protected void onRenderOriginChanged() {
		transformManager.clearInstancers();
	}

	@Override
	public void delete() {
		transformManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
		info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
	}
}
