package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityInstanceManager extends InstanceManager<Entity> {

	public EntityInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(Entity obj) {
		return obj != null && InstancedRenderRegistry.canInstance(obj.getType());
	}

	@Override
	protected AbstractInstance createRaw(Entity obj) {
		return InstancedRenderRegistry.createInstance(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) {
			return false;
		}

		Level world = entity.level();

		return Backend.isFlywheelWorld(world);

	}
}
