package com.jozufozu.flywheel.backend.instancing.blockentity;

import java.util.List;

import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityInstanceManager extends InstanceManager<BlockEntity> {

	private final Long2ObjectMap<BlockEntityInstance<?>> posLookup = new Long2ObjectOpenHashMap<>();

	public BlockEntityInstanceManager(InstancerManager instancerManager) {
		super(instancerManager);
	}

	public void getCrumblingInstances(long pos, List<BlockEntityInstance<?>> data) {
		BlockEntityInstance<?> instance = posLookup.get(pos);
		if (instance != null) {
			data.add(instance);
		}
	}

	@Override
	protected boolean canInstance(BlockEntity obj) {
		return obj != null && InstancedRenderRegistry.canInstance(obj.getType());
	}

	@Override
	protected AbstractInstance createRaw(BlockEntity obj) {
		var instance = InstancedRenderRegistry.createInstance(instancerManager, obj);

		if (instance != null) {
			BlockPos blockPos = obj.getBlockPos();
			posLookup.put(blockPos.asLong(), instance);
		}

		return instance;
	}

	@Override
	protected void removeInternal(BlockEntity obj, AbstractInstance instance) {
		super.removeInternal(obj, instance);
		posLookup.remove(obj.getBlockPos().asLong());
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
