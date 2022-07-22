package com.jozufozu.flywheel.core.crumbling;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstanceManager;

public class CrumblingInstanceManager extends BlockEntityInstanceManager {

	public CrumblingInstanceManager(InstancerManager instancerManager) {
		super(instancerManager);
	}

	@Override
	protected void updateInstance(DynamicInstance dyn, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		dyn.beginFrame();
	}
}
