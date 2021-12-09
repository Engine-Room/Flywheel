package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.backend.api.MaterialManager;

import net.minecraft.core.BlockPos;

public class CrumblingInstanceManager extends TileInstanceManager {

	public CrumblingInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}
}
