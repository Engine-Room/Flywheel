package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class EntityInstanceManager extends InstanceManager<Entity> {

	public EntityInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(Entity obj) {
		return obj != null && InstancedRenderRegistry.getInstance().canInstance(obj.getType());
	}

	@Override
	protected AbstractInstance createRaw(Entity obj) {
		return InstancedRenderRegistry.getInstance()
				.create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) return false;

		Level world = entity.level;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = entity.blockPosition();

			BlockGetter existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
