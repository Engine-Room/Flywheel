package dev.engine_room.flywheel.api.event;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is posted to the Forge event bus.
 */
public final class RenderStageEvent extends Event {
	private final RenderContext context;
	private final RenderStage stage;

	public RenderStageEvent(RenderContext context, RenderStage stage) {
		this.context = context;
		this.stage = stage;
	}

	public RenderContext context() {
		return context;
	}

	public RenderStage stage() {
		return stage;
	}

	public ClientLevel level() {
		return context.level();
	}

	public PoseStack stack() {
		return context.stack();
	}

	public Matrix4f viewProjection() {
		return context.viewProjection();
	}

	public RenderBuffers buffers() {
		return context.buffers();
	}

	public Camera camera() {
		return context.camera();
	}

	@Override
	public String toString() {
		return "RenderStageEvent{" + context + "}";
	}
}
