package com.jozufozu.flywheel.backend.instancing;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.state.RenderLayer;
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

	private static final WorldAttached<InstanceWorld> instanceWorlds = new WorldAttached<>($ -> new InstanceWorld());

	@NotNull
	public static InstanceManager<TileEntity> getTiles(IWorld world) {
		return instanceWorlds.get(world)
				.getTileEntityInstanceManager();
	}

	@NotNull
	public static InstanceManager<Entity> getEntities(IWorld world) {
		return instanceWorlds.get(world)
				.getEntityInstanceManager();
	}

	public static void tick(Minecraft mc) {

		if (!Backend.isGameActive()) {
			return;
		}
		ClientWorld world = mc.world;
		AnimationTickHolder.tick();

		instanceWorlds.get(world).tick();
	}

	public static void enqueueUpdate(TileEntity te) {
		getTiles(te.getLevel()).queueUpdate(te);
	}

	public static void enqueueUpdate(Entity entity) {
		getEntities(entity.level).queueUpdate(entity);
	}

	public static void onBeginFrame(BeginFrameEvent event) {
		instanceWorlds.get(event.getWorld()).beginFrame(event);
	}

	public static void renderLayer(RenderLayerEvent event) {
		if (event.layer == null) return;

		ClientWorld world = event.getWorld();
		if (!Backend.getInstance()
				.canUseInstancing(world)) return;

		instanceWorlds.get(world).renderLayer(event);
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientWorld world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			loadAllInWorld(world);
		}
	}

	public static void loadAllInWorld(ClientWorld world) {
		instanceWorlds.replace(world, InstanceWorld::delete)
				.loadAll(world);
	}

}
