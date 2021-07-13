package com.jozufozu.flywheel.backend.instancing;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class InstancedRenderDispatcher {

	private static final WorldAttached<MaterialManager<WorldProgram>> materialManagers = new WorldAttached<>($ -> new MaterialManager<>(Contexts.WORLD));

	private static final WorldAttached<InstanceManager<Entity>> entityInstanceManager = new WorldAttached<>(world -> new EntityInstanceManager(materialManagers.get(world)));
	private static final WorldAttached<InstanceManager<TileEntity>> tileInstanceManager = new WorldAttached<>(world -> new TileInstanceManager(materialManagers.get(world)));

	@NotNull
	public static InstanceManager<TileEntity> getTiles(IWorld world) {
		return tileInstanceManager.get(world);
	}

	@NotNull
	public static InstanceManager<Entity> getEntities(IWorld world) {
		return entityInstanceManager.get(world);
	}

	public static void tick(Minecraft mc) {

		if (!Backend.isGameActive()) {
			return;
		}
		ClientWorld world = mc.world;
		AnimationTickHolder.tick();

		Entity renderViewEntity = mc.renderViewEntity != null ? mc.renderViewEntity : mc.player;

		if (renderViewEntity == null) return;

		getTiles(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		getEntities(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	public static void enqueueUpdate(TileEntity te) {
		getTiles(te.getWorld()).queueUpdate(te);
	}

	public static void enqueueUpdate(Entity entity) {
		getEntities(entity.world).queueUpdate(entity);
	}

	public static void onBeginFrame(BeginFrameEvent event) {
		materialManagers.get(event.getWorld())
				.checkAndShiftOrigin(event.getInfo());

		getTiles(event.getWorld()).beginFrame(event.getInfo());
		getEntities(event.getWorld()).beginFrame(event.getInfo());
	}

	public static void renderLayer(RenderLayerEvent event) {
		ClientWorld world = event.getWorld();
		if (!Backend.getInstance()
				.canUseInstancing(world)) return;

		event.type.startDrawing();

		materialManagers.get(world)
				.render(event.type, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.endDrawing();
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientWorld world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			loadAllInWorld(world);
		}
	}

	public static void loadAllInWorld(ClientWorld world) {
		materialManagers.get(world)
				.delete();

		InstanceManager<TileEntity> tiles = tileInstanceManager.replace(world);
		world.loadedTileEntityList.forEach(tiles::add);

		InstanceManager<Entity> entities = entityInstanceManager.replace(world);
		world.getAllEntities()
				.forEach(entities::add);
	}
}
