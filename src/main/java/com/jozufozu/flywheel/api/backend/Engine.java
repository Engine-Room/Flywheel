package com.jozufozu.flywheel.api.backend;

import java.util.List;

import com.jozufozu.flywheel.api.BackendImplemented;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visualization.EmbeddedLevel;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

@BackendImplemented
public interface Engine {
	/**
	 * Get an instancer for the given instance type, model, and render stage.
	 *
	 * <p>Calling this method twice with the same arguments will return the same instancer.</p>
	 *
	 * <p>If you are writing a visual you should probably be using
	 * {@link InstancerProvider#instancer(InstanceType, Model)}, which will decide the {@code RenderStage}
	 * based on what type of visual is getting the instancer.</p>
	 *
	 * @return An instancer for the given instance type, model, and render stage.
	 * @see InstancerProvider
	 */
	<I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model, RenderStage stage);

	<I extends Instance> Instancer<I> instancer(EmbeddedLevel world, InstanceType<I> type, Model model, RenderStage stage);

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
	 * <br>
	 * This is guaranteed to be called between the first and last calls to {@link #renderStage} for the current frame.
	 *
	 * @param executor        The task executor running the frame plan.
	 * @param context         The render context for this frame.
	 * @param crumblingBlocks The instances to render. This list is never empty.
	 */
	void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks);

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

	/**
	 * A block to be rendered as a crumbling overlay.
	 * @param progress The progress of the crumbling animation in the range [0, 10).
	 * @param pos The position of the block.
	 * @param instances The instances associated with the BE at this position.
	 */
	record CrumblingBlock(int progress, BlockPos pos, List<Instance> instances) {
	}
}
