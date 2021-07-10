package com.jozufozu.flywheel.event;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class EntityWorldHandler {

	public static void onEntityJoinWorld(Entity entity, ClientWorld world) {
		InstancedRenderDispatcher.getEntities(world).queueAdd(entity);
	}

	public static void onEntityLeaveWorld(Entity entity, ClientWorld world) {
		InstancedRenderDispatcher.getEntities(world).remove(entity);
	}
}
