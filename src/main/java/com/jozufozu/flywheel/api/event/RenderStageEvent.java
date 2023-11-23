package com.jozufozu.flywheel.api.event;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraftforge.eventbus.api.Event;

public class RenderStageEvent extends Event {
	private final RenderContext context;
	private final RenderStage stage;

	public RenderStageEvent(RenderContext context, RenderStage stage) {
		this.context = context;
		this.stage = stage;
	}

	public RenderContext getContext() {
		return context;
	}

	public RenderStage getStage() {
		return stage;
	}

	public ClientLevel getLevel() {
		return context.level();
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

	public Camera getCamera() {
		return context.camera();
	}

	@Override
	public String toString() {
		return "RenderStageEvent{" + context + "}";
	}
}
