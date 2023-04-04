package com.jozufozu.flywheel.backend.instancing.manager;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.BlockEntityInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.backend.instancing.storage.One2OneStorage;
import com.jozufozu.flywheel.backend.instancing.storage.Storage;
import com.jozufozu.flywheel.lib.instance.InstancingControllerHelper;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityInstanceManager extends InstanceManager<BlockEntity> {
	private final BlockEntityStorage storage;

	public BlockEntityInstanceManager(InstancerProvider instancerManager) {
		storage = new BlockEntityStorage(instancerManager);
	}

	@Override
	protected Storage<BlockEntity> getStorage() {
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

		if (!InstancingControllerHelper.canInstance(blockEntity.getType())) {
			return false;
		}

		Level level = blockEntity.getLevel();

		if (level == null) {
			return false;
		}

		if (level.isEmptyBlock(blockEntity.getBlockPos())) {
			return false;
		}

		if (BackendUtil.isFlywheelLevel(level)) {
			BlockPos pos = blockEntity.getBlockPos();

			BlockGetter existingChunk = level.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}

	private static class BlockEntityStorage extends One2OneStorage<BlockEntity> {
		private final Long2ObjectMap<BlockEntityInstance<?>> posLookup = new Long2ObjectOpenHashMap<>();

		public BlockEntityStorage(InstancerProvider instancerManager) {
			super(instancerManager);
		}

		@Override
		@Nullable
		protected Instance createRaw(BlockEntity obj) {
			BlockEntityInstance<?> instance = InstancingControllerHelper.createInstance(instancerManager, obj);

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
