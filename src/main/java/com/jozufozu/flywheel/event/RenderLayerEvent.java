package com.jozufozu.flywheel.event;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.fabric.event.EventContext;
import com.jozufozu.flywheel.fabric.helper.Matrix4fHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;

public class RenderLayerEvent extends EventContext {
	private final ClientLevel world;
	public final RenderType type;
	public final PoseStack stack;
	public final Matrix4f viewProjection;
	public final RenderBuffers buffers;
	public final double camX;
	public final double camY;
	public final double camZ;
	public final RenderLayer layer;

	public RenderLayerEvent(ClientLevel world, RenderType type, PoseStack stack, RenderBuffers buffers, double camX, double camY, double camZ) {
		this.world = world;
		this.type = type;
		this.stack = stack;

		viewProjection = stack.last()
				.pose()
				.copy();
        Matrix4fHelper.multiplyBackward(viewProjection, RenderSystem.getProjectionMatrix());

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

	public ClientLevel getWorld() {
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
