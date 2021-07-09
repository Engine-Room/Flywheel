package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.fabricmc.api.EnvType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(EnvType.CLIENT)
public class EntityWorldHandler {

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote) InstancedRenderDispatcher.getEntities(event.getWorld())
				.queueAdd(event.getEntity());
	}

	@SubscribeEvent
	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
		if (event.getWorld().isRemote) InstancedRenderDispatcher.getEntities(event.getWorld())
				.remove(event.getEntity());
	}
}
