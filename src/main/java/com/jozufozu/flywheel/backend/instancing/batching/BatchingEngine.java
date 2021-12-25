package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.api.MaterialGroup;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.SuperBufferSource;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class BatchingEngine implements Engine {

	private final Map<RenderLayer, Map<RenderType, BatchedMaterialGroup>> layers;
	private final SuperBufferSource superBufferSource = new SuperBufferSource();

	public BatchingEngine() {
		this.layers = new EnumMap<>(RenderLayer.class);
		for (RenderLayer value : RenderLayer.values()) {
			layers.put(value, new HashMap<>());
		}

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
	public void render(TaskEngine taskEngine, RenderLayerEvent event) {

		Map<RenderType, BatchedMaterialGroup> groups = layers.get(event.getLayer());
		for (BatchedMaterialGroup group : groups.values()) {
			group.render(event.stack, superBufferSource, taskEngine);
		}

		taskEngine.syncPoint();

		// FIXME: this probably breaks some vanilla stuff but it works much better for flywheel
		Matrix4f mat = new Matrix4f();
		mat.setIdentity();
		if (event.getWorld().effects().constantAmbientLight()) {
			Lighting.setupNetherLevel(mat);
		} else {
			Lighting.setupLevel(mat);
		}

		superBufferSource.endBatch();
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
