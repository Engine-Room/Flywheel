package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class EntityWorldHandler {

	public static void onEntityJoinWorld(Entity entity, ClientLevel world) {
		InstancedRenderDispatcher.getEntities(world).queueAdd(entity);
	}

	public static void onEntityLeaveWorld(Entity entity, ClientLevel world) {
		InstancedRenderDispatcher.getEntities(world).remove(entity);
	}
}
