package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.backend.instancing.manager.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.manager.EffectInstanceManager;
import com.jozufozu.flywheel.backend.instancing.manager.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.manager.InstanceManager;
import com.jozufozu.flywheel.backend.task.ParallelTaskExecutor;
import com.jozufozu.flywheel.extension.ClientLevelExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 * <p>
 *     The instancer manager is shared between the different instance managers.
 * </p>
 */
public class InstanceWorld implements AutoCloseable {
	protected final Engine engine;
	protected final InstanceManager<Entity> entities;
	protected final InstanceManager<BlockEntity> blockEntities;

	public final ParallelTaskExecutor taskExecutor;
	private final InstanceManager<Effect> effects;

	public static InstanceWorld create(LevelAccessor level) {
		var engine = BackendManager.getBackend()
				.createEngine();

		var entities = new EntityInstanceManager(engine);
		var blockEntities = new BlockEntityInstanceManager(engine);
		var effects = new EffectInstanceManager(engine);

		engine.attachManagers(entities, blockEntities, effects);

		return new InstanceWorld(engine, entities, blockEntities, effects);
	}

	public InstanceWorld(Engine engine, InstanceManager<Entity> entities, InstanceManager<BlockEntity> blockEntities,
			InstanceManager<Effect> effects) {
		this.engine = engine;
		this.entities = entities;
		this.blockEntities = blockEntities;
		this.effects = effects;
		this.taskExecutor = BackendUtil.getTaskExecutor();
	}

	public InstanceManager<Entity> getEntities() {
		return entities;
	}

	public InstanceManager<Effect> getEffects() {
		return effects;
	}

	public InstanceManager<BlockEntity> getBlockEntities() {
		return blockEntities;
	}

	/**
	 * Free all acquired resources and invalidate this instance world.
	 */
	public void delete() {
		engine.delete();
		entities.delete();
		blockEntities.delete();
	}

	/**
	 * Get ready to render a frame.
	 * <p>
	 *     Check and shift the origin coordinate.
	 *     <br>
	 *     Call {@link DynamicInstance#beginFrame()} on all instances in this world.
	 * </p>
	 */
	public void beginFrame(BeginFrameEvent event) {
		RenderContext context = event.getContext();
		boolean shifted = engine.maintainOriginCoordinate(context.camera());

		taskExecutor.syncPoint();

		if (!shifted) {
			blockEntities.beginFrame(taskExecutor, context);
			entities.beginFrame(taskExecutor, context);
			effects.beginFrame(taskExecutor, context);
		}

		engine.beginFrame(taskExecutor, context);
	}

	/**
	 * Tick the renderers after the game has ticked:
	 * <p>
	 *     Call {@link TickableInstance#tick()} on all instances in this world.
	 * </p>
	 */
	public void tick() {
		Minecraft mc = Minecraft.getInstance();

		if (mc.isPaused()) return;

		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		double x = renderViewEntity.getX();
		double y = renderViewEntity.getY();
		double z = renderViewEntity.getZ();

		blockEntities.tick(taskExecutor, x, y, z);
		entities.tick(taskExecutor, x, y, z);
		effects.tick(taskExecutor, x, y, z);
	}

	/**
	 * Draw all instances for the given stage.
	 */
	public void renderStage(RenderContext context, RenderStage stage) {
		taskExecutor.syncPoint();
		engine.renderStage(taskExecutor, context, stage);
	}

	/**
	 * Instantiate all the necessary instances to render the given world.
	 */
	public void loadEntities(ClientLevel level) {
		// Block entities are loaded while chunks are baked.
		// Entities are loaded with the level, so when chunks are reloaded they need to be re-added.
		ClientLevelExtension.getAllLoadedEntities(level)
				.forEach(entities::add);
	}

	@Override
	public void close() {
		delete();
	}
}
