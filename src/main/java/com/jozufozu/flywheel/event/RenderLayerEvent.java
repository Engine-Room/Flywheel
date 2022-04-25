package com.jozufozu.flywheel.event;

import org.jetbrains.annotations.NotNull;

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
	public final RenderType type;

	public RenderLayerEvent(RenderContext context, RenderType type) {
		this.context = context;
		this.type = type;
	}

	@NotNull
	public static Matrix4f createViewProjection(PoseStack view) {
		var viewProjection = view.last()
				.pose()
				.copy();
		viewProjection.multiplyBackward(RenderSystem.getProjectionMatrix());
		return viewProjection;
	}

	@Override
	public String toString() {
		return "RenderLayerEvent{" + context + "}";
	}

	public ClientLevel getWorld() {
		return context.level();
	}

	public RenderType getType() {
		return type;
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
