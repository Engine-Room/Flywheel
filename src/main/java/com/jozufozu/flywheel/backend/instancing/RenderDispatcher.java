package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.core.RenderContext;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;

public interface RenderDispatcher {

	void renderAllRemaining(TaskEngine taskEngine, RenderContext context);

	void renderSpecificType(TaskEngine taskEngine, RenderContext context, RenderType type);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions.
	 * <p>
	 * This prevents floating point precision issues at high coordinates.
	 */
	void beginFrame(Camera info);

	void delete();
}
