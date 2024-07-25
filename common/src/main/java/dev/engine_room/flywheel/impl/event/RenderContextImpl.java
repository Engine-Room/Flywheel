package dev.engine_room.flywheel.impl.event;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

public record RenderContextImpl(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack,
								Matrix4fc projection, Matrix4fc viewProjection, Camera camera,
								float partialTick) implements RenderContext {
	public static RenderContextImpl create(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack, Matrix4f projection, Camera camera, float partialTick) {
		Matrix4f viewProjection = new Matrix4f(projection);
		viewProjection.mul(stack.last()
				.pose());

		return new RenderContextImpl(renderer, level, buffers, stack, projection, viewProjection, camera, partialTick);
	}
}
