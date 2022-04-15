package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.core.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;

public interface RenderDispatcher {
	/**
	 * Render every model for every material.
	 */
	void render(TaskEngine taskEngine, RenderContext context);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 * <p>
	 * This prevents floating point precision issues at high coordinates.
	 */
	void beginFrame(Camera info);

	void delete();
}
