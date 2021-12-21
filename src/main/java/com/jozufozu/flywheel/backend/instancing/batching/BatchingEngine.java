package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class BatchingEngine implements Engine {

	protected final Map<RenderLayer, Map<RenderType, BatchedMaterialGroup>> layers;
	protected final TaskEngine taskEngine;

	public BatchingEngine(TaskEngine taskEngine) {
		this.layers = new EnumMap<>(RenderLayer.class);
		for (RenderLayer value : RenderLayer.values()) {
			layers.put(value, new HashMap<>());
		}

		this.taskEngine = taskEngine;
	}

	@Override
	public MaterialGroup state(RenderLayer layer, RenderType state) {
		return layers.get(layer).computeIfAbsent(state, BatchedMaterialGroup::new);
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return BlockPos.ZERO;
	}

	@Override
	public void render(RenderLayerEvent event, MultiBufferSource buffers) {
		PoseStack stack = event.stack;

		stack.pushPose();

		stack.translate(-event.camX, -event.camY, -event.camZ);

		for (BatchedMaterialGroup group : layers.get(event.getLayer()).values()) {
			group.render(stack, buffers, taskEngine);
		}

		taskEngine.syncPoint();

		stack.popPose();

		event.buffers.bufferSource().endBatch();
	}

	@Override
	public void delete() {
	}

	@Override
	public void beginFrame(Camera info) {

	}

	@Override
	public String getName() {
		return "Batching";
	}
}
