package com.jozufozu.flywheel.backend.engine.batching;

import java.util.List;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class BatchingEngine implements Engine {
	protected final BatchingTransformManager transformManager = new BatchingTransformManager();
	protected final BatchingDrawTracker drawTracker = new BatchingDrawTracker();

	@Override
	public <D extends InstancedPart> Instancer<D> instancer(StructType<D> type, Model model, RenderStage stage) {
		return transformManager.getInstancer(type, model, stage);
	}

	@Override
	public void beginFrame(TaskExecutor executor, RenderContext context) {
		transformManager.flush();

		Vec3 cameraPos = context.camera().getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		// TODO: async task executor barriers
		executor.syncPoint();
		submitTasks(executor, stack.last(), context.level());
	}

	public void submitTasks(TaskExecutor executor, PoseStack.Pose matrices, ClientLevel level) {
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
	public boolean maintainOriginCoordinate(Camera camera) {
		// do nothing
		return false;
	}

	@Override
	public void attachManagers(InstanceManager<?>... listener) {
		// noop
	}

	@Override
	public Vec3i getOriginCoordinate() {
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
