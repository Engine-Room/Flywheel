package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class InstancedRenderDispatcher {

	private static final WorldAttached<InstanceWorld> instanceWorlds = new WorldAttached<>($ -> new InstanceWorld());

	/**
	 * Call this when you want to manually run {@link AbstractInstance#update()}.
	 * @param te The tile whose instance you want to update.
	 */
	public static void enqueueUpdate(BlockEntity te) {
		if (te.hasLevel() && te.getLevel() instanceof ClientLevel)
			getTiles(te.getLevel()).queueUpdate(te);
	}

	/**
	 * Call this when you want to manually run {@link AbstractInstance#update()}.
	 * @param entity The entity whose instance you want to update.
	 */
	public static void enqueueUpdate(Entity entity) {
		getEntities(entity.level).queueUpdate(entity);
	}

	public static InstanceManager<BlockEntity> getTiles(LevelAccessor world) {
		return instanceWorlds.get(world)
				.getTileEntityInstanceManager();
	}

	public static InstanceManager<Entity> getEntities(LevelAccessor world) {
		return instanceWorlds.get(world)
				.getEntityInstanceManager();
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {

		if (!Backend.isGameActive() || event.phase == TickEvent.Phase.START) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientLevel world = mc.level;
		AnimationTickHolder.tick();

		instanceWorlds.get(world).tick();
	}

	@SubscribeEvent
	public static void onBeginFrame(BeginFrameEvent event) {
		if (Backend.isGameActive()) {
			instanceWorlds.get(event.getWorld())
					.beginFrame(event);
		}
	}

	@SubscribeEvent
	public static void renderLayer(RenderLayerEvent event) {
		if (event.layer == null) return;

		ClientLevel world = event.getWorld();
		if (!Backend.getInstance()
				.canUseInstancing(world)) return;

		instanceWorlds.get(world).renderLayer(event);
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientLevel world = event.getWorld();
		if (Backend.getInstance()
				.canUseInstancing() && world != null) {
			resetInstanceWorld(world);
		}
	}

	public static void resetInstanceWorld(ClientLevel world) {
		instanceWorlds.replace(world, InstanceWorld::delete);
	}

}
