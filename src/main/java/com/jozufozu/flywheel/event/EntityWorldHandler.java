package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class EntityWorldHandler {

	public static void onEntityJoinWorld(Entity entity, ClientLevel level) {
		InstancedRenderDispatcher.getEntities(level)
				.queueAdd(entity);
	}

	public static void onEntityLeaveWorld(Entity entity, ClientLevel level) {
		InstancedRenderDispatcher.getEntities(level)
				.remove(entity);
	}
}
