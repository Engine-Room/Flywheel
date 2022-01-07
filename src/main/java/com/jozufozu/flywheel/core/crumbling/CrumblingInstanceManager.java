package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;

import net.minecraft.core.BlockPos;

public class CrumblingInstanceManager extends BlockEntityInstanceManager {

	public CrumblingInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}
}
