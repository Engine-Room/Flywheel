package com.jozufozu.flywheel.backend.instancing.blockentity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityInstanceManager extends InstanceManager<BlockEntity> {

	public BlockEntityInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(BlockEntity obj) {
		return obj != null && InstancedRenderRegistry.canInstance(obj.getType());
	}

	@Override
	protected AbstractInstance createRaw(BlockEntity obj) {
		return InstancedRenderRegistry.createInstance(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(BlockEntity blockEntity) {
		if (blockEntity.isRemoved()) return false;

		Level world = blockEntity.getLevel();

		if (world == null) return false;

		if (world.isEmptyBlock(blockEntity.getBlockPos())) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = blockEntity.getBlockPos();

			BlockGetter existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
