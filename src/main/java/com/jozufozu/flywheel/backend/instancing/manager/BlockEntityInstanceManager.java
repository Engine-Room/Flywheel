package com.jozufozu.flywheel.backend.instancing.manager;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.BlockEntityInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instance.controller.InstancingControllerRegistry;
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

	public BlockEntityInstanceManager(Engine engine) {
		storage = new BlockEntityStorage(engine);
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

		public BlockEntityStorage(Engine engine) {
			super(engine);
		}

		@Override
		@Nullable
		protected Instance createRaw(BlockEntity obj) {
			var controller = InstancingControllerRegistry.getController(InstancingControllerHelper.getType(obj));
			if (controller == null) {
				return null;
			}

			var out = controller.createInstance(new InstanceContext(engine, engine.renderOrigin()), obj);

			BlockPos blockPos = obj.getBlockPos();
			posLookup.put(blockPos.asLong(), out);

			return out;
		}

		@Override
		public void remove(BlockEntity obj) {
			super.remove(obj);
			posLookup.remove(obj.getBlockPos().asLong());
		}
	}
}
