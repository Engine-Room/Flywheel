package com.jozufozu.flywheel.handler;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;

public class EntityWorldHandler {

	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide && Backend.isOn()) InstancedRenderDispatcher.getEntities(event.getWorld())
				.queueAdd(event.getEntity());
	}

	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
		if (event.getWorld().isClientSide && Backend.isOn()) InstancedRenderDispatcher.getEntities(event.getWorld())
				.remove(event.getEntity());
	}
}
