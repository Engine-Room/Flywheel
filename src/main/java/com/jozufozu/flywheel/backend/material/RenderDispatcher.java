package com.jozufozu.flywheel.backend.material;

import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.event.RenderLayerEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderDispatcher {
	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which of the 3 {@link RenderLayer render layers} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 */
	void render(RenderLayerEvent event, MultiBufferSource buffers);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 * <p>
	 * This prevents floating point precision issues at high coordinates.
	 */
	void beginFrame(Camera info);

	default void delete() {

	}
}
