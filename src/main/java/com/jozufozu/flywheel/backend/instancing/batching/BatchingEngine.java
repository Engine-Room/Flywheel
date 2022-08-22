package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.RenderContext;
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
	protected final Map<StructType<?>, CPUInstancerFactory<?>> factories = new HashMap<>();

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public <D extends InstancedPart> CPUInstancerFactory<D> factory(StructType<D> type) {
		return (CPUInstancerFactory<D>) factories.computeIfAbsent(type, this::createFactory);
	}

	@NotNull
	private <D extends InstancedPart> CPUInstancerFactory<D> createFactory(StructType<D> type) {
		return new CPUInstancerFactory<>(type, transformManager::create);
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		transformManager.flush();

		Vec3 cameraPos = context.camera().getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		// TODO: async task engine barriers
		taskEngine.syncPoint();
		submitTasks(taskEngine, stack, context.level());
	}

	public void submitTasks(TaskEngine taskEngine, PoseStack stack, ClientLevel level) {
		BatchingTransformManager.TransformSet transformSet = transformManager.get(RenderStage.AFTER_FINAL_END_BATCH);
		for (var entry : transformSet) {
			var renderType = entry.getKey();
			var transformCalls = entry.getValue();

			int vertices = 0;
			for (var transformCall : transformCalls) {
				vertices += transformCall.getTotalVertexCount();
			}

			if (vertices == 0) {
				continue;
			}

			DrawBuffer buffer = drawTracker.getBuffer(renderType);
			buffer.prepare(vertices);

			int startVertex = 0;
			for (var transformCall : transformCalls) {
				transformCall.submitTasks(taskEngine, buffer, startVertex, stack, level);
				startVertex += transformCall.getTotalVertexCount();
			}
		}
	}

	@Override
	public void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage) {
		// FIXME: properly support material stages
		// This also breaks block outlines on batched block entities
		// and makes translucent blocks occlude everything Flywheel renders
		if (stage != RenderStage.AFTER_FINAL_END_BATCH) {
			return;
		}

		drawTracker.drawAll();
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
		factories.clear();
		transformManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
	}
}
