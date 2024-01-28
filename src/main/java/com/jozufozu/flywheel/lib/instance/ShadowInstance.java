package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

public class ShadowInstance extends AbstractInstance {

	public float x, y, z;
	public float entityX, entityZ;
	public float sizeX, sizeZ;
	public float alpha;
	public float radius;

	public ShadowInstance(InstanceType<?> type, InstanceHandle handle) {
		super(type, handle);
	}
}
