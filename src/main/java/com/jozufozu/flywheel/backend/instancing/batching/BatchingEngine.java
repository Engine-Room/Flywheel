package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.backend.instancing.BatchDrawingTracker;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class BatchingEngine implements Engine {

	private final Map<StructType<?>, CPUInstancerFactory<?>> factories = new HashMap<>();
	private final BatchDrawingTracker batchTracker = new BatchDrawingTracker();

	private final BatchLists batchLists = new BatchLists();

	protected final List<BatchedModel<?>> uninitializedModels = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Override
	public <D extends InstancedPart> CPUInstancerFactory<D> factory(StructType<D> type) {
		return (CPUInstancerFactory<D>) factories.computeIfAbsent(type, this::createFactory);
	}

	public <D extends InstancedPart> CPUInstancerFactory<D> createFactory(StructType<D> type) {
		return new CPUInstancerFactory<>(type, uninitializedModels::add);
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return BlockPos.ZERO;
	}

	public void submitTasks(PoseStack stack, TaskEngine taskEngine) {
		batchLists.renderLists.forEach((renderType, renderList) -> {
			int vertices = 0;
			for (var transformSet : renderList) {
				vertices += transformSet.getTotalVertexCount();
			}

			var consumer = batchTracker.getDirectConsumer(renderType, vertices);
			consumer.memSetZero();

			var outputColorDiffuse = !consumer.hasOverlay() && !OptifineHandler.isUsingShaders();

			for (var transformSet : renderList) {
				transformSet.setOutputColorDiffuse(outputColorDiffuse);
				transformSet.submitTasks(stack, taskEngine, consumer);
			}
		});
	}

	@Override
	public void renderSpecificType(TaskEngine taskEngine, RenderContext context, RenderType type) {

	}

	@Override
	public void renderAllRemaining(TaskEngine taskEngine, RenderContext context) {

		// FIXME: this probably breaks some vanilla stuff but it works much better for flywheel
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		if (context.level().effects().constantAmbientLight()) {
			Lighting.setupNetherLevel(mat);
		} else {
			Lighting.setupLevel(mat);
		}

		taskEngine.syncPoint();
		batchTracker.endBatch();
	}

	@Override
	public void delete() {
	}

	@Override
	public boolean maintainOriginCoordinate(Camera camera) {
		// do nothing
		return false;
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, Camera info) {
		for (var model : uninitializedModels) {
			model.init(batchLists);
		}

		uninitializedModels.clear();

		Vec3 cameraPos = info.getPosition();
		var stack = FlwUtil.copyPoseStack(RenderContext.CURRENT.stack());
		stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		submitTasks(stack, taskEngine);
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("Batching");
		info.add("Instances: " + 0);
		info.add("Vertices: " + 0);
	}
}
