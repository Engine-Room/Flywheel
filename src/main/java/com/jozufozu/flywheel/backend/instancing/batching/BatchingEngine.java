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

	protected final BatchingDrawManager drawManager = new BatchingDrawManager();
	protected final Map<StructType<?>, CPUInstancerFactory<?>> factories = new HashMap<>();

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public <D extends InstancedPart> CPUInstancerFactory<D> factory(StructType<D> type) {
		return (CPUInstancerFactory<D>) factories.computeIfAbsent(type, this::createFactory);
	}

	@NotNull
	private <D extends InstancedPart> CPUInstancerFactory<D> createFactory(StructType<D> type) {
		return new CPUInstancerFactory<>(type, drawManager::create);
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		drawManager.flush();

		Vec3 cameraPos = context.camera().getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		submitTasks(taskEngine, stack, context.level());
	}

	public void submitTasks(TaskEngine taskEngine, PoseStack stack, ClientLevel level) {
		BatchingDrawManager.TransformSet drawSet = drawManager.get(RenderStage.AFTER_FINAL_END_BATCH);
		for (var entry : drawSet) {
			var renderType = entry.getKey();
			var renderList = entry.getValue();

			int vertices = 0;
			for (var transformSet : renderList) {
				vertices += transformSet.getTotalVertexCount();
			}

			DrawBuffer buffer = drawManager.batchTracker.getBuffer(renderType);
			buffer.prepare(vertices);

			int startVertex = 0;
			for (var transformSet : renderList) {
				transformSet.submitTasks(taskEngine, buffer, startVertex, stack, level);
				startVertex += transformSet.getTotalVertexCount();
			}
		};
	}

	@Override
	public void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage) {
		// FIXME: properly support material stages
		// This also breaks block outlines on batched block entities
		// and makes translucent blocks occlude everything Flywheel renders
		if (stage != RenderStage.AFTER_FINAL_END_BATCH) {
			return;
		}

		drawManager.batchTracker.endBatch();
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
		drawManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
	}
}
