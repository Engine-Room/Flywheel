package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.core.RenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.Event;

public class RenderLayerEvent extends Event {
	public final RenderContext context;

	public RenderLayerEvent(ClientLevel world, RenderType type, PoseStack stack, RenderBuffers buffers, double camX, double camY, double camZ) {
		var viewProjection = stack.last()
				.pose()
				.copy();
        viewProjection.multiplyBackward(RenderSystem.getProjectionMatrix());

		context = new RenderContext(world, type, stack, viewProjection, buffers, camX, camY, camZ);
	}

	@Override
	public String toString() {
		return "RenderLayerEvent{" + context + "}";
	}

	public ClientLevel getWorld() {
		return context.level();
	}

	public RenderType getType() {
		return context.type();
	}

	public PoseStack getStack() {
		return context.stack();
	}

	public Matrix4f getViewProjection() {
		return context.viewProjection();
	}

	public RenderBuffers getBuffers() {
		return context.buffers();
	}

	public double getCamX() {
		return context.camX();
	}

	public double getCamY() {
		return context.camY();
	}

	public double getCamZ() {
		return context.camZ();
	}
}
