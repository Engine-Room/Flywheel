package com.jozufozu.flywheel.backend.instancing.blockentity;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.One2OneStorage;
import com.jozufozu.flywheel.backend.instancing.Storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityInstanceManager extends InstanceManager<BlockEntity> {

	private final BlockEntityStorage storage;

	public BlockEntityInstanceManager(InstancerManager instancerManager) {
		storage = new BlockEntityStorage(instancerManager);
	}

	@Override
	public Storage<BlockEntity> getStorage() {
		return storage;
	}

	public void getCrumblingInstances(long pos, List<BlockEntityInstance<?>> data) {
		BlockEntityInstance<?> instance = storage.posLookup.get(pos);
		if (instance != null) {
			data.add(instance);
		}
	}

	@Override
	protected boolean canCreateInstance(BlockEntity blockEntity) {
		if (blockEntity.isRemoved()) {
			return false;
		}

		if (!InstancedRenderRegistry.canInstance(blockEntity.getType())) {
			return false;
		}

		Level world = blockEntity.getLevel();

		if (world == null) {
			return false;
		}

		if (world.isEmptyBlock(blockEntity.getBlockPos())) {
			return false;
		}

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = blockEntity.getBlockPos();

			BlockGetter existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}

	public static class BlockEntityStorage extends One2OneStorage<BlockEntity> {

		final Long2ObjectMap<BlockEntityInstance<?>> posLookup = new Long2ObjectOpenHashMap<>();


		public BlockEntityStorage(InstancerManager manager) {
			super(manager);
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
		public void remove(BlockEntity obj) {
			super.remove(obj);
			posLookup.remove(obj.getBlockPos().asLong());
		}
	}
}
