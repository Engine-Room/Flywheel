package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;

import com.jozufozu.flywheel.core.Contexts;

import net.minecraft.util.math.BlockPos;

public class CrumblingInstanceManager extends TileInstanceManager {
	public CrumblingInstanceManager() {
		super(new CrumblingMaterialManager());
	}

	@Override
	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}
}
