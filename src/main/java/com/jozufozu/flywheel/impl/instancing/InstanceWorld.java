package com.jozufozu.flywheel.impl.instancing;

import java.util.List;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.backend.task.FlwTaskExecutor;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.instancing.manager.BlockEntityInstanceManager;
import com.jozufozu.flywheel.impl.instancing.manager.EffectInstanceManager;
import com.jozufozu.flywheel.impl.instancing.manager.EntityInstanceManager;
import com.jozufozu.flywheel.impl.instancing.manager.InstanceManager;
import com.jozufozu.flywheel.lib.task.PlanUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 */
// AutoCloseable is implemented to prevent leaking this object from WorldAttached
public class InstanceWorld implements AutoCloseable {
	private final Engine engine;
	private final ParallelTaskExecutor taskExecutor;

	private final InstanceManager<BlockEntity> blockEntities;
	private final InstanceManager<Entity> entities;
	private final InstanceManager<Effect> effects;

	public InstanceWorld(LevelAccessor level) {
		engine = BackendManager.getBackend().createEngine(level);
		taskExecutor = FlwTaskExecutor.get();

		blockEntities = new BlockEntityInstanceManager(engine);
		entities = new EntityInstanceManager(engine);
		effects = new EffectInstanceManager(engine);
	}

	public Engine getEngine() {
		return engine;
	}

	public InstanceManager<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	public InstanceManager<Entity> getEntities() {
		return entities;
	}

	public InstanceManager<Effect> getEffects() {
		return effects;
	}

	/**
	 * Tick the instances after the game has ticked:
	 * <p>
	 *     Call {@link TickableInstance#tick()} on all instances in this world.
	 * </p>
	 */
	public void tick(double cameraX, double cameraY, double cameraZ) {
		taskExecutor.syncPoint();

		blockEntities.planThisTick(cameraX, cameraY, cameraZ)
				.and(entities.planThisTick(cameraX, cameraY, cameraZ))
				.and(effects.planThisTick(cameraX, cameraY, cameraZ))
				.maybeSimplify()
				.execute(taskExecutor);
	}

	/**
	 * Get ready to render a frame.
	 * <p>
	 *     Check and update the render origin.
	 *     <br>
	 *     Call {@link DynamicInstance#beginFrame()} on all instances in this world.
	 * </p>
	 */
	public void beginFrame(RenderContext context) {
		taskExecutor.syncPoint();

		getManagerPlan(context).then(engine.planThisFrame(context))
				.maybeSimplify()
				.execute(taskExecutor);
	}

	private Plan getManagerPlan(RenderContext context) {
		if (engine.updateRenderOrigin(context.camera())) {
			return PlanUtil.of(blockEntities::recreateAll, entities::recreateAll, effects::recreateAll);
		} else {
			var cameraPos = context.camera()
					.getPosition();
			double cameraX = cameraPos.x;
			double cameraY = cameraPos.y;
			double cameraZ = cameraPos.z;
			FrustumIntersection culler = context.culler();

			return PlanUtil.of(blockEntities.planThisFrame(cameraX, cameraY, cameraZ, culler), entities.planThisFrame(cameraX, cameraY, cameraZ, culler), effects.planThisFrame(cameraX, cameraY, cameraZ, culler));
		}
	}

	/**
	 * Draw all instances for the given stage.
	 */
	public void renderStage(RenderContext context, RenderStage stage) {
		engine.renderStage(taskExecutor, context, stage);
	}

	public void addDebugInfo(List<String> info) {
		info.add("B: " + blockEntities.getInstanceCount()
				+ ", E: " + entities.getInstanceCount()
				+ ", F: " + effects.getInstanceCount());
		info.add("Update limiting: " + FlwCommands.boolToText(FlwConfig.get().limitUpdates()).getString());
		engine.addDebugInfo(info);
	}

	/**
	 * Free all acquired resources and invalidate this instance world.
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
}
