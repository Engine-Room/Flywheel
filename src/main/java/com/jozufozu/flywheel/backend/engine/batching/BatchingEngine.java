package com.jozufozu.flywheel.backend.engine.batching;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.PlanUtil;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class BatchingEngine implements Engine {
	private final BatchingTransformManager transformManager = new BatchingTransformManager();
	private final BatchingDrawTracker drawTracker = new BatchingDrawTracker();

	@Override
	public <P extends InstancePart> Instancer<P> instancer(StructType<P> type, Model model, RenderStage stage) {
		return transformManager.getInstancer(type, model, stage);
	}

	@Override
	public Plan planThisFrame(RenderContext context) {
		return PlanUtil.of(transformManager::flush)
				.then(planTransformers(context));
	}

	private Plan planTransformers(RenderContext context) {
		Vec3 cameraPos = context.camera()
				.getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		var matrices = stack.last();
		var level = context.level();

		var plans = new ArrayList<Plan>();

		for (var transformSetEntry : transformManager.getTransformSetsView()
				.entrySet()) {
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
					plans.add(transformCall.getPlan(buffer, startVertex, matrices, level));
					startVertex += transformCall.getTotalVertexCount();
				}
			}
		}

		return new NestedPlan(plans);
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		if (!drawTracker.hasStage(stage)) {
			return;
		}
		executor.syncPoint();
		drawTracker.draw(stage);
	}

	@Override
	public boolean updateRenderOrigin(Camera camera) {
		return false;
	}

	@Override
	public Vec3i renderOrigin() {
		return BlockPos.ZERO;
	}

	@Override
	public void delete() {
		transformManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
	}
}
