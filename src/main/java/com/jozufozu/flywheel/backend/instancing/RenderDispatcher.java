package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.event.RenderLayerEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderDispatcher {
	/**
	 * Render every model for every material.
	 *
	 * @param event     Context for rendering.
	 * @param buffers	The buffer source for which batched rendering should happen.
	 */
	void render(RenderLayerEvent event, MultiBufferSource buffers);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 * <p>
	 * This prevents floating point precision issues at high coordinates.
	 */
	void beginFrame(Camera info);

	void delete();
}
