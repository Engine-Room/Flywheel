package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileInstanceManager extends InstanceManager<BlockEntity> {

	public TileInstanceManager(MaterialManager<?> materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(BlockEntity obj) {
		return obj != null && InstancedRenderRegistry.getInstance().canInstance(obj.getType());
	}

	@Override
	protected IInstance createRaw(BlockEntity obj) {
		return InstancedRenderRegistry.getInstance()
				.create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(BlockEntity tile) {
		if (tile.isRemoved()) return false;

		Level world = tile.getLevel();

		if (world == null) return false;

		if (world.isEmptyBlock(tile.getBlockPos())) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = tile.getBlockPos();

			BlockGetter existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
