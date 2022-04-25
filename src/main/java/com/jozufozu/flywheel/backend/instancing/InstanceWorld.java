package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.batching.BatchingEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.util.ClientLevelExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 * <p>
 *     The material manager is shared between the different instance managers.
 * </p>
 */
public class InstanceWorld {
	protected final Engine engine;
	protected final InstanceManager<Entity> entityInstanceManager;
	protected final InstanceManager<BlockEntity> blockEntityInstanceManager;

	public final ParallelTaskEngine taskEngine;

	public static InstanceWorld create(LevelAccessor level) {
		return switch (Backend.getBackendType()) {
		case INSTANCING -> {
			InstancingEngine<WorldProgram> manager = new InstancingEngine<>(Contexts.WORLD);

			var entityInstanceManager = new EntityInstanceManager(manager);
			var blockEntityInstanceManager = new BlockEntityInstanceManager(manager);

			manager.addListener(entityInstanceManager);
			manager.addListener(blockEntityInstanceManager);
			yield new InstanceWorld(manager, entityInstanceManager, blockEntityInstanceManager);
		}
		case BATCHING -> {
			var manager = new BatchingEngine();
			var entityInstanceManager = new EntityInstanceManager(manager);
			var blockEntityInstanceManager = new BlockEntityInstanceManager(manager);

			yield new InstanceWorld(manager, entityInstanceManager, blockEntityInstanceManager);
		}
		default -> throw new IllegalArgumentException("Unknown engine type");
		};
	}

	public InstanceWorld(Engine engine, InstanceManager<Entity> entityInstanceManager, InstanceManager<BlockEntity> blockEntityInstanceManager) {
		this.engine = engine;
		this.entityInstanceManager = entityInstanceManager;
		this.blockEntityInstanceManager = blockEntityInstanceManager;
		this.taskEngine = Backend.getTaskEngine();
	}

	public InstanceManager<Entity> getEntityInstanceManager() {
		return entityInstanceManager;
	}

	public InstanceManager<BlockEntity> getBlockEntityInstanceManager() {
		return blockEntityInstanceManager;
	}

	/**
	 * Free all acquired resources and invalidate this instance world.
	 */
	public void delete() {
		engine.delete();
		entityInstanceManager.detachLightListeners();
		blockEntityInstanceManager.detachLightListeners();
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
		engine.beginFrame(event.getCamera());

		taskEngine.syncPoint();

		blockEntityInstanceManager.beginFrame(taskEngine, event.getCamera());
		entityInstanceManager.beginFrame(taskEngine, event.getCamera());
	}

	/**
	 * Tick the renderers after the game has ticked:
	 * <p>
	 *     Call {@link TickableInstance#tick()} on all instances in this world.
	 * </p>
	 */
	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		blockEntityInstanceManager.tick(taskEngine, renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		entityInstanceManager.tick(taskEngine, renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	/**
	 * Draw the given layer.
	 */
	public void renderSpecificType(RenderContext context, RenderType type) {
		taskEngine.syncPoint();
		context.pushPose();
		context.translateBack(context.camX(), context.camY(), context.camZ());
		engine.renderSpecificType(taskEngine, context, type);
		context.popPose();
	}

	/**
	 * Draw the given layer.
	 */
	public void renderAllRemaining(RenderContext context) {
		taskEngine.syncPoint();
		context.pushPose();
		context.translateBack(context.camX(), context.camY(), context.camZ());
		engine.renderAllRemaining(taskEngine, context);
		context.popPose();
	}

	/**
	 * Instantiate all the necessary instances to render the given world.
	 */
	public void loadEntities(ClientLevel world) {
		// Block entities are loaded while chunks are baked.
		// Entities are loaded with the world, so when chunks are reloaded they need to be re-added.
		ClientLevelExtension.cast(world)
				.flywheel$getAllLoadedEntities()
				.forEach(entityInstanceManager::add);
	}

}
