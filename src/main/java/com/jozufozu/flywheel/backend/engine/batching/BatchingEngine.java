package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.util.FlwUtil;

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
	public Plan planThisFrame(RenderContext context) {
		Vec3 cameraPos = context.camera()
				.getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(renderOrigin.getX() - cameraPos.x, renderOrigin.getY() - cameraPos.y, renderOrigin.getZ() - cameraPos.z);

		return transformManager.plan(stack.last(), context.level(), drawTracker);
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
