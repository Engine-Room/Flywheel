package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.batching.BatchingEngine;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.effect.Effect;
import com.jozufozu.flywheel.backend.instancing.effect.EffectInstanceManager;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.util.ClientLevelExtension;
import com.jozufozu.flywheel.vanilla.effect.ExampleEffect;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 * <p>
 *     The instancer manager is shared between the different instance managers.
 * </p>
 */
public class InstanceWorld {
	protected final Engine engine;
	protected final InstanceManager<Entity> entities;
	protected final InstanceManager<BlockEntity> blockEntities;

	public final ParallelTaskEngine taskEngine;
	private final InstanceManager<Effect> effects;

	public static InstanceWorld create(LevelAccessor level) {
		var engine = switch (Backend.getBackendType()) {
			case INSTANCING -> new InstancingEngine<>(Contexts.WORLD);
			case BATCHING -> new BatchingEngine();
			case OFF -> throw new IllegalStateException("Cannot create instance world when backend is off.");
		};

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
		this.taskEngine = Backend.getTaskEngine();
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
		Camera camera = event.getCamera();
		boolean shifted = engine.maintainOriginCoordinate(camera);

		taskEngine.syncPoint();

		if (!shifted) {
			blockEntities.beginFrame(taskEngine, camera);
			entities.beginFrame(taskEngine, camera);
			effects.beginFrame(taskEngine, camera);
		}

		engine.beginFrame(taskEngine, camera);
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

		double x = renderViewEntity.getX();
		double y = renderViewEntity.getY();
		double z = renderViewEntity.getZ();

		blockEntities.tick(taskEngine, x, y, z);
		entities.tick(taskEngine, x, y, z);
		effects.tick(taskEngine, x, y, z);
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
		ClientLevelExtension.getAllLoadedEntities(world)
				.forEach(entities::add);
	}

}
