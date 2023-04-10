package com.jozufozu.flywheel.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

public record RenderContext(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack,
							Matrix4f projection, Matrix4f viewProjection, Camera camera) {
	public static RenderContext create(LevelRenderer renderer, ClientLevel level, RenderBuffers buffers, PoseStack stack, Matrix4f projection, Camera camera) {
		Matrix4f viewProjection = projection.copy();
		viewProjection.multiply(stack.last().pose());

		return new RenderContext(renderer, level, buffers, stack, projection, viewProjection, camera);
	}
}
