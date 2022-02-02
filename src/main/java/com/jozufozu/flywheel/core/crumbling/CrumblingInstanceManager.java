package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;

public class CrumblingInstanceManager extends BlockEntityInstanceManager {

	public CrumblingInstanceManager(MaterialManager materialManager) {
		super(materialManager);
	}

	@Override
	protected void updateInstance(DynamicInstance dyn, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		dyn.beginFrame();
	}
}
