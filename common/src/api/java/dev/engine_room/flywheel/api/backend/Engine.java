package dev.engine_room.flywheel.api.backend;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

import dev.engine_room.flywheel.api.BackendImplemented;
import dev.engine_room.flywheel.api.RenderContext;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LightLayer;

@BackendImplemented
public interface Engine {
	/**
	 * Create a visualization context that will be used to create visuals of the given type.
	 *
	 * @param visualType The type of visual.
	 * @return A new visualization context.
	 */
	VisualizationContext createVisualizationContext(VisualType visualType);

	/**
	 * Create a plan that will start execution after the start of the level render and
	 * finish execution before {@link #setupRender} is called.
	 *
	 * @return A new plan.
	 */
	Plan<RenderContext> createFramePlan();

	/**
	 * @return The current render origin.
	 */
	Vec3i renderOrigin();

	/**
	 * Maintain the render origin to be within a certain distance from the camera in all directions,
	 * preventing floating point precision issues at high coordinates.
	 *
	 * @return {@code true} if the render origin changed, {@code false} otherwise.
	 */
	boolean updateRenderOrigin(Camera camera);

	/**
	 * Assign the set of sections that visuals have requested GPU light for.
	 *
	 * <p> This will be called at most once per frame, and not necessarily every frame.
	 *
	 * @param sections The set of sections.
	 */
	void lightSections(LongSet sections);

	void onLightUpdate(SectionPos sectionPos, LightLayer layer);

	/**
	 * Set up rendering for the current level render.
	 *
	 * <p>This method is guaranteed to be called after
	 * {@linkplain #createFramePlan() the frame plan} has finished execution and before
	 * {@link #render} and {@link #renderCrumbling} are called. This method is guaranteed to
	 * be called on the render thread.
	 *
	 * @param context The context for the current level render.
	 */
	void setupRender(RenderContext context);

	/**
	 * Render all instances necessary for the given visual type.
	 *
	 * <p>This method is guaranteed to be called after {@link #setupRender} for the current
	 * level render. This method is guaranteed to be called on the render thread.
	 *
	 * @param context The context for the current level render.
	 * @param visualType The type of visual.
	 */
	void render(RenderContext context, VisualType visualType);

	/**
	 * Render the given instances as a crumbling overlay.
	 *
	 * <p>This method is guaranteed to be called after {@link #setupRender} for the current
	 * level render. This method is guaranteed to be called on the render thread.
	 *
	 * @param context The context for the current level render.
	 * @param crumblingBlocks The instances to render. This list is never empty.
	 */
	void renderCrumbling(RenderContext context, List<CrumblingBlock> crumblingBlocks);

	/**
	 * Free all resources associated with this engine.
	 *
	 * <p>This engine will not be used again after this method is called.
	 *
	 * <p>This method is guaranteed to be called on the render thread.
	 */
	void delete();

	/**
	 * A block to be rendered as a crumbling overlay.
	 */
	@ApiStatus.NonExtendable
	interface CrumblingBlock {
		/**
		 * The position of the block.
		 */
		BlockPos pos();

		/**
		 * The progress of the crumbling animation in the range [0, 10).
		 */
		@Range(from = 0, to = 9)
		int progress();

		/**
		 * The instances associated with the block entity visual at this position.
		 */
		List<Instance> instances();
	}
}
