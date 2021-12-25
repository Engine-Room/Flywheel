package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.event.RenderLayerEvent;

import net.minecraft.client.Camera;

public interface RenderDispatcher {
	/**
	 * Render every model for every material.
	 *
	 * @param taskEngine
	 * @param event     	Context for rendering.
	 */
	void render(TaskEngine taskEngine, RenderLayerEvent event);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 * <p>
	 * This prevents floating point precision issues at high coordinates.
	 */
	void beginFrame(Camera info);

	void delete();
}
