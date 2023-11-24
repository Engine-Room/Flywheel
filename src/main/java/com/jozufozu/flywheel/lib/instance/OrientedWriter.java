package com.jozufozu.flywheel.lib.instance;

import org.lwjgl.system.MemoryUtil;

public class OrientedWriter extends ColoredLitWriter<OrientedInstance> {
	public static final OrientedWriter INSTANCE = new OrientedWriter();

	@Override
	public void write(long ptr, OrientedInstance instance) {
		super.write(ptr, instance);

		MemoryUtil.memPutFloat(ptr + 8, instance.posX);
		MemoryUtil.memPutFloat(ptr + 12, instance.posY);
		MemoryUtil.memPutFloat(ptr + 16, instance.posZ);
		MemoryUtil.memPutFloat(ptr + 20, instance.pivotX);
		MemoryUtil.memPutFloat(ptr + 24, instance.pivotY);
		MemoryUtil.memPutFloat(ptr + 28, instance.pivotZ);
		MemoryUtil.memPutFloat(ptr + 32, instance.rotation.x);
		MemoryUtil.memPutFloat(ptr + 36, instance.rotation.y);
		MemoryUtil.memPutFloat(ptr + 40, instance.rotation.z);
		MemoryUtil.memPutFloat(ptr + 44, instance.rotation.w);
	}

}
