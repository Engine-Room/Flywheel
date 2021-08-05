package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 * <br>
 * The material manager is shared between the different instance managers.
 */
public class InstanceWorld {
	protected final MaterialManager<WorldProgram> materialManager;
	protected final InstanceManager<Entity> entityInstanceManager;
	protected final InstanceManager<BlockEntity> BlockEntityInstanceManager;

	public InstanceWorld() {

		materialManager = MaterialManager.builder(Contexts.WORLD)
				.build();
		entityInstanceManager = new EntityInstanceManager(materialManager);
		BlockEntityInstanceManager = new TileInstanceManager(materialManager);
	}

	public MaterialManager<WorldProgram> getMaterialManager() {
		return materialManager;
	}

	public InstanceManager<Entity> getEntityInstanceManager() {
		return entityInstanceManager;
	}

	public InstanceManager<BlockEntity> getBlockEntityInstanceManager() {
		return BlockEntityInstanceManager;
	}

	/**
	 * Free all acquired resources and invalidate this instance world.
	 */
	public void delete() {
		materialManager.delete();
	}

	/**
	 * Instantiate all the necessary instances to render the given world.
	 */
	public void loadAll(ClientLevel world) {
		world.blockEntityList.forEach(BlockEntityInstanceManager::add);
		world.entitiesForRendering()
				.forEach(entityInstanceManager::add);
	}

	/**
	 * Get ready to render a frame:
	 * <br>
	 * Check and shift the origin coordinate.
	 * <br>
	 * Call {@link IDynamicInstance#beginFrame()} on all instances in this world.
	 */
	public void beginFrame(BeginFrameEvent event) {
		materialManager.checkAndShiftOrigin(event.getInfo());

		BlockEntityInstanceManager.beginFrame(event.getInfo());
		entityInstanceManager.beginFrame(event.getInfo());
	}

	/**
	 * Tick the renderers after the game has ticked:
	 * <br>
	 * Call {@link ITickableInstance#tick()} on all instances in this world.
	 */
	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		BlockEntityInstanceManager.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		entityInstanceManager.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	/**
	 * Draw the given layer.
	 */
	public void renderLayer(RenderLayerEvent event) {
		event.type.setupRenderState();

		materialManager.render(event.layer, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.clearRenderState();
	}
}
