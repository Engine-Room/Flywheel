package dev.engine_room.flywheel.api.backend;

import java.util.List;

import dev.engine_room.flywheel.api.BackendImplemented;
import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

@BackendImplemented
public interface Engine {
	/**
	 * Create a visualization context that will render to the given stage.
	 *
	 * @param stage The stage to render to.
	 * @return A new visualization context.
	 */
	VisualizationContext createVisualizationContext(RenderStage stage);

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
	 * Assign the set of sections that visuals have requested GPU light for.
	 *
	 * @param sections The set of sections.
	 */
	void lightSections(LongSet sections);

	/**
	 * A block to be rendered as a crumbling overlay.
	 * @param progress The progress of the crumbling animation in the range [0, 10).
	 * @param pos The position of the block.
	 * @param instances The instances associated with the BE at this position.
	 */
	record CrumblingBlock(int progress, BlockPos pos, List<Instance> instances) {
	}
}
