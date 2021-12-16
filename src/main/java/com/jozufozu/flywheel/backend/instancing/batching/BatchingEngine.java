package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class BatchingEngine implements Engine {

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final Map<RenderLayer, Map<RenderType, BatchedMaterialGroup>> layers;

	private final BatchExecutor pool;

	public BatchingEngine() {
		this.layers = new EnumMap<>(RenderLayer.class);
		for (RenderLayer value : RenderLayer.values()) {
			layers.put(value, new HashMap<>());
		}

		pool = new BatchExecutor(Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism()));
	}

	@Override
	public MaterialGroup state(RenderLayer layer, RenderType state) {
		return layers.get(layer).computeIfAbsent(state, BatchedMaterialGroup::new);
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return originCoordinate;
	}

	@Override
	public void render(RenderLayerEvent event, MultiBufferSource buffers) {
		PoseStack stack = event.stack;

		stack.pushPose();

		stack.translate(-event.camX, -event.camY, -event.camZ);

		for (Map.Entry<RenderType, BatchedMaterialGroup> entry : layers.get(event.getLayer()).entrySet()) {
			BatchedMaterialGroup group = entry.getValue();

			group.render(stack, buffers, pool);
		}

		try {
			pool.await();
		} catch (InterruptedException ignored) {
		}

		stack.popPose();
	}

	@Override
	public void beginFrame(Camera info) {

	}

	@Override
	public String getName() {
		return "Batching";
	}
}
