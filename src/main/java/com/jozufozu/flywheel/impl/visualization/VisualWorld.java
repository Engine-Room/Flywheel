package com.jozufozu.flywheel.impl.visualization;

import java.util.List;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.backend.task.FlwTaskExecutor;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.TickContext;
import com.jozufozu.flywheel.impl.visualization.manager.BlockEntityVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.EffectVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.EntityVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.VisualManager;
import com.jozufozu.flywheel.lib.math.MatrixUtil;
import com.jozufozu.flywheel.lib.task.NestedPlan;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.util.Unit;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 */
// AutoCloseable is implemented to prevent leaking this object from WorldAttached
public class VisualWorld implements AutoCloseable {
	private final Engine engine;
	private final ParallelTaskExecutor taskExecutor;

	private final VisualManager<BlockEntity> blockEntities;
	private final VisualManager<Entity> entities;
	private final VisualManager<Effect> effects;

	private final Plan<TickContext> tickPlan;
	private final Plan<RenderContext> framePlan;

	public VisualWorld(LevelAccessor level) {
		engine = BackendManager.getBackend()
				.createEngine(level);
		taskExecutor = FlwTaskExecutor.get();

		blockEntities = new BlockEntityVisualManager(engine);
		entities = new EntityVisualManager(engine);
		effects = new EffectVisualManager(engine);

		tickPlan = blockEntities.createTickPlan()
				.and(entities.createTickPlan())
				.and(effects.createTickPlan())
				.maybeSimplify();
		framePlan = new FramePlan();
	}

	public Engine getEngine() {
		return engine;
	}

	public VisualManager<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	public VisualManager<Entity> getEntities() {
		return entities;
	}

	public VisualManager<Effect> getEffects() {
		return effects;
	}

	/**
	 * Tick the visuals after the game has ticked:
	 * <p>
	 * Call {@link TickableVisual#tick} on all visuals in this world.
	 * </p>
	 */
	public void tick(double cameraX, double cameraY, double cameraZ) {
		taskExecutor.syncPoint();

		tickPlan.execute(taskExecutor, new TickContext(cameraX, cameraY, cameraZ));
	}

	/**
	 * Get ready to render a frame.
	 * <p>
	 *     Check and update the render origin.
	 *     <br>
	 *     Call {@link DynamicVisual#beginFrame} on all visuals in this world.
	 * </p>
	 */
	public void beginFrame(RenderContext context) {
		taskExecutor.syncPoint();

		framePlan.execute(taskExecutor, context);
	}

	/**
	 * Draw all visuals for the given stage.
	 */
	public void renderStage(RenderContext context, RenderStage stage) {
		engine.renderStage(taskExecutor, context, stage);
	}

	public void addDebugInfo(List<String> info) {
		info.add("B: " + blockEntities.getVisualCount()
				+ ", E: " + entities.getVisualCount()
				+ ", F: " + effects.getVisualCount());
		info.add("Update limiting: " + FlwCommands.boolToText(FlwConfig.get().limitUpdates()).getString());
		engine.addDebugInfo(info);
	}

	/**
	 * Free all acquired resources and invalidate this visual world.
	 */
	public void delete() {
		taskExecutor.discardAndAwait();
		blockEntities.invalidate();
		entities.invalidate();
		effects.invalidate();
		engine.delete();
	}

	@Override
	public void close() {
		delete();
	}

	private class FramePlan implements SimplyComposedPlan<RenderContext> {
		private final Plan<Unit> recreationPlan = NestedPlan.of(blockEntities.createRecreationPlan(), entities.createRecreationPlan(), effects.createRecreationPlan());
		private final Plan<FrameContext> normalPlan = blockEntities.createFramePlan()
				.and(entities.createFramePlan())
				.and(effects.createFramePlan());

		private final Plan<RenderContext> enginePlan = engine.createFramePlan();

		@Override
		public void execute(TaskExecutor taskExecutor, RenderContext context, Runnable onCompletion) {
			Runnable then = () -> enginePlan.execute(taskExecutor, context, onCompletion);

			if (engine.updateRenderOrigin(context.camera())) {
				recreationPlan.execute(taskExecutor, Unit.INSTANCE, then);
			} else {
				Vec3i renderOrigin = engine.renderOrigin();
				var cameraPos = context.camera()
						.getPosition();
				double cameraX = cameraPos.x;
				double cameraY = cameraPos.y;
				double cameraZ = cameraPos.z;

				org.joml.Matrix4f proj = MatrixUtil.toJoml(context.viewProjection());
				proj.translate((float) (renderOrigin.getX() - cameraX), (float) (renderOrigin.getY() - cameraY), (float) (renderOrigin.getZ() - cameraZ));
				FrustumIntersection frustum = new FrustumIntersection(proj);

				var frameContext = new FrameContext(cameraX, cameraY, cameraZ, frustum);

				normalPlan.execute(taskExecutor, frameContext, then);
			}
		}
	}
}
