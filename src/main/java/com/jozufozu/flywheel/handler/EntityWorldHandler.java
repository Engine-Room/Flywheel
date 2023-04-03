package com.jozufozu.flywheel.handler;

import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;

public class EntityWorldHandler {
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide && BackendManager.isOn()) InstancedRenderDispatcher.getEntities(event.getWorld())
				.queueAdd(event.getEntity());
	}

	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
		if (event.getWorld().isClientSide && BackendManager.isOn()) InstancedRenderDispatcher.getEntities(event.getWorld())
				.remove(event.getEntity());
	}
}
