package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.material.MaterialManagerImpl;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TileInstanceManager extends InstanceManager<TileEntity> {

	public TileInstanceManager(MaterialManagerImpl<?> materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean canInstance(TileEntity obj) {
		return obj != null && InstancedRenderRegistry.getInstance().canInstance(obj.getType());
	}

	@Override
	protected AbstractInstance createRaw(TileEntity obj) {
		return InstancedRenderRegistry.getInstance()
				.create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(TileEntity tile) {
		if (tile.isRemoved()) return false;

		World world = tile.getLevel();

		if (world == null) return false;

		if (world.isEmptyBlock(tile.getBlockPos())) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = tile.getBlockPos();

			IBlockReader existingChunk = world.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
