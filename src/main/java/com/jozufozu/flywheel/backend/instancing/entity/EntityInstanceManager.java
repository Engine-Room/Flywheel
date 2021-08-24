package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class EntityInstanceManager extends InstanceManager<Entity> {

	public EntityInstanceManager(MaterialManagerImpl<?> materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(Entity obj) {
		return obj != null && InstancedRenderRegistry.getInstance().canInstance(obj.getType());
	}

	@Override
	protected IInstance createRaw(Entity obj) {
		return InstancedRenderRegistry.getInstance()
				.create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) return false;

		World world = entity.level;

		if (world == null) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = entity.blockPosition();

			IBlockReader existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
