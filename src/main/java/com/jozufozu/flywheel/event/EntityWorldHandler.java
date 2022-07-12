package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EntityWorldHandler {

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide && Backend.isOn()) InstancedRenderDispatcher.getEntities(event.getLevel())
				.queueAdd(event.getEntity());
	}

	@SubscribeEvent
	public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
		if (event.getLevel().isClientSide && Backend.isOn()) InstancedRenderDispatcher.getEntities(event.getLevel())
				.remove(event.getEntity());
	}
}
