package com.jozufozu.flywheel.api.backend;

import java.util.List;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;

import net.minecraft.client.Camera;
import net.minecraft.core.Vec3i;

public interface Engine extends InstancerProvider {
	/**
	 * Create a plan that will be executed every frame.
	 * @return A new plan.
	 */
	Plan<RenderContext> createFramePlan();

	/**
	 * Render all instances necessary for the given stage.
	 * @param executor The task executor running the frame plan.
	 * @param context The render context for this frame.
	 * @param stage The stage to render.
	 */
	void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage);

	/**
	 * Render the given instances as a crumbling overlay.
	 * @param taskExecutor The task executor running the frame plan.
	 * @param context The render context for this frame.
	 * @param instances The instances to render.
	 * @param progress The progress of the crumbling animation, i.e. which texture to use.
	 */
	void renderCrumblingInstances(TaskExecutor taskExecutor, RenderContext context, List<Instance> instances, int progress);

	/**
	 * Maintain the render origin to be within a certain distance from the camera in all directions,
	 * preventing floating point precision issues at high coordinates.
	 *
	 * @return {@code true} if the render origin changed, {@code false} otherwise.
	 */
	boolean updateRenderOrigin(Camera camera);

	/**
	 * @return The current render origin.
	 */
	Vec3i renderOrigin();

	/**
	 * Free all resources associated with this engine.
	 * <br>
	 * This engine will not be used again after this method is called.
	 */
	void delete();
}
