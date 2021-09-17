package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A manager class for a single world where instancing is supported.
 * <p>
 *     The material manager is shared between the different instance managers.
 * </p>
 */
public class InstanceWorld {
	protected final MaterialManagerImpl<WorldProgram> materialManager;
	protected final InstanceManager<Entity> entityInstanceManager;
	protected final InstanceManager<BlockEntity> tileEntityInstanceManager;

	public InstanceWorld() {

		materialManager = MaterialManagerImpl.builder(Contexts.WORLD)
				.build();
		entityInstanceManager = new EntityInstanceManager(materialManager);
		tileEntityInstanceManager = new TileInstanceManager(materialManager);
	}

	public MaterialManager getMaterialManager() {
		return materialManager;
	}

	public InstanceManager<Entity> getEntityInstanceManager() {
		return entityInstanceManager;
	}

	public InstanceManager<BlockEntity> getTileEntityInstanceManager() {
		return tileEntityInstanceManager;
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
		// FIXME: no more global blockEntity list
		// world.blockEntityList.forEach(tileEntityInstanceManager::add);
		world.entitiesForRendering()
				.forEach(entityInstanceManager::add);
	}

	/**
	 * Get ready to render a frame.
	 * <p>
	 *     Check and shift the origin coordinate.
	 *     <br>
	 *     Call {@link IDynamicInstance#beginFrame()} on all instances in this world.
	 * </p>
	 */
	public void beginFrame(BeginFrameEvent event) {
		materialManager.beginFrame(event.getInfo());

		tileEntityInstanceManager.beginFrame(event.getInfo());
		entityInstanceManager.beginFrame(event.getInfo());
	}

	/**
	 * Tick the renderers after the game has ticked:
	 * <p>
	 *     Call {@link ITickableInstance#tick()} on all instances in this world.
	 * </p>
	 */
	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		tileEntityInstanceManager.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		entityInstanceManager.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	/**
	 * Draw the given layer.
	 */
	public void renderLayer(RenderLayerEvent event) {
		event.type.setupRenderState();

		ShaderInstance shader = RenderSystem.getShader();
		if (shader != null)
			shader.apply();

		materialManager.render(event.layer, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.clearRenderState();
	}
}
