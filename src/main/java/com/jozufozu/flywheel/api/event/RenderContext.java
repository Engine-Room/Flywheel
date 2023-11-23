package com.jozufozu.flywheel.api.event;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

public record RenderContext(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack,
							Matrix4f projection, Matrix4f viewProjection, Camera camera, float partialTick) {
	public static RenderContext create(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack, Matrix4f projection, Camera camera, float partialTick) {
		Matrix4f viewProjection = new Matrix4f(projection);
		viewProjection.mul(stack.last()
				.pose());

		return new RenderContext(renderer, level, buffers, stack, projection, viewProjection, camera, partialTick);
	}
}
