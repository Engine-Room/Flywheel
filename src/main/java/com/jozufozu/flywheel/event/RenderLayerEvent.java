package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.fabric.event.EventContext;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderLayerEvent extends EventContext {
	private final ClientWorld world;
	public final RenderType type;
	public final MatrixStack stack;
	public final Matrix4f viewProjection;
	public final RenderTypeBuffers buffers;
	public final double camX;
	public final double camY;
	public final double camZ;
	public final RenderLayer layer;

	public RenderLayerEvent(ClientWorld world, RenderType type, MatrixStack stack, RenderTypeBuffers buffers, double camX, double camY, double camZ) {
		this.world = world;
		this.type = type;
		this.stack = stack;

		viewProjection = stack.last()
				.pose()
				.copy();
		viewProjection.multiplyBackward(Backend.getInstance()
				.getProjectionMatrix());

		this.buffers = buffers;
		this.camX = camX;
		this.camY = camY;
		this.camZ = camZ;

		this.layer = RenderLayer.fromRenderType(type);
	}

	@Nullable
	public RenderLayer getLayer() {
		return layer;
	}

	public ClientWorld getWorld() {
		return world;
	}

	public RenderType getType() {
		return type;
	}

	public Matrix4f getViewProjection() {
		return viewProjection;
	}

	public double getCamX() {
		return camX;
	}

	public double getCamY() {
		return camY;
	}

	public double getCamZ() {
		return camZ;
	}
}
