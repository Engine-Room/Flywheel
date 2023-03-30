package com.jozufozu.flywheel.backend.instancing.entity;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.storage.One2OneStorage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityInstanceManager extends InstanceManager<Entity> {

	private final One2OneStorage<Entity> storage;

	public EntityInstanceManager(InstancerManager instancerManager) {
		storage = new One2OneStorage<>(instancerManager) {
			@Override
			protected @Nullable AbstractInstance createRaw(Entity obj) {
				return InstancedRenderRegistry.createInstance(this.instancerManager, obj);
			}
		};
	}

	@Override
	public One2OneStorage<Entity> getStorage() {
		return storage;
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) {
			return false;
		}

		if (!InstancedRenderRegistry.canInstance(entity.getType())) {
			return false;
		}

		Level level = entity.level;

		return Backend.isFlywheelLevel(level);
	}
}
