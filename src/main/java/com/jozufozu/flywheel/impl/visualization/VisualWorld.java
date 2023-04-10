package com.jozufozu.flywheel.impl.visualization;

import java.util.List;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.backend.task.FlwTaskExecutor;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.visualization.manager.BlockEntityVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.EffectVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.EntityVisualManager;
import com.jozufozu.flywheel.impl.visualization.manager.VisualManager;
import com.jozufozu.flywheel.lib.math.MatrixUtil;

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

	public VisualWorld(LevelAccessor level) {
		engine = BackendManager.getBackend().createEngine(level);
		taskExecutor = FlwTaskExecutor.get();

		blockEntities = new BlockEntityVisualManager(engine);
		entities = new EntityVisualManager(engine);
		effects = new EffectVisualManager(engine);
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
	 *     Call {@link TickableVisual#tick()} on all visuals in this world.
	 * </p>
	 */
	public void tick(double cameraX, double cameraY, double cameraZ) {
		blockEntities.tick(taskExecutor, cameraX, cameraY, cameraZ);
		entities.tick(taskExecutor, cameraX, cameraY, cameraZ);
		effects.tick(taskExecutor, cameraX, cameraY, cameraZ);
	}

	/**
	 * Get ready to render a frame.
	 * <p>
	 *     Check and update the render origin.
	 *     <br>
	 *     Call {@link DynamicVisual#beginFrame()} on all visuals in this world.
	 * </p>
	 */
	public void beginFrame(RenderContext context) {
		boolean originChanged = engine.updateRenderOrigin(context.camera());

		if (originChanged) {
			blockEntities.recreateAll();
			entities.recreateAll();
			effects.recreateAll();
		}

		taskExecutor.syncPoint();

		if (!originChanged) {
			Vec3i renderOrigin = engine.renderOrigin();
			var cameraPos = context.camera()
					.getPosition();
			double cameraX = cameraPos.x;
			double cameraY = cameraPos.y;
			double cameraZ = cameraPos.z;

			org.joml.Matrix4f proj = MatrixUtil.toJoml(context.viewProjection());
			proj.translate((float) (renderOrigin.getX() - cameraX), (float) (renderOrigin.getY() - cameraY), (float) (renderOrigin.getZ() - cameraZ));
			FrustumIntersection frustum = new FrustumIntersection(proj);

			blockEntities.beginFrame(taskExecutor, cameraX, cameraY, cameraZ, frustum);
			entities.beginFrame(taskExecutor, cameraX, cameraY, cameraZ, frustum);
			effects.beginFrame(taskExecutor, cameraX, cameraY, cameraZ, frustum);
		}

		engine.beginFrame(taskExecutor, context);
	}

	/**
	 * Draw all visuals for the given stage.
	 */
	public void renderStage(RenderContext context, RenderStage stage) {
		taskExecutor.syncPoint();
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
}
