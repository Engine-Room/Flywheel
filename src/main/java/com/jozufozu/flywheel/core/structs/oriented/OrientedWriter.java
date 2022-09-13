package com.jozufozu.flywheel.core.structs.oriented;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.core.structs.ColoredLitWriter;

public class OrientedWriter extends ColoredLitWriter<OrientedPart> {
	public static final OrientedWriter INSTANCE = new OrientedWriter();

	@Override
	public void write(long ptr, OrientedPart d) {
		super.write(ptr, d);

		MemoryUtil.memPutFloat(ptr + 8, d.posX);
		MemoryUtil.memPutFloat(ptr + 12, d.posY);
		MemoryUtil.memPutFloat(ptr + 16, d.posZ);
		MemoryUtil.memPutFloat(ptr + 20, d.pivotX);
		MemoryUtil.memPutFloat(ptr + 24, d.pivotY);
		MemoryUtil.memPutFloat(ptr + 28, d.pivotZ);
		MemoryUtil.memPutFloat(ptr + 32, d.qX);
		MemoryUtil.memPutFloat(ptr + 36, d.qY);
		MemoryUtil.memPutFloat(ptr + 40, d.qZ);
		MemoryUtil.memPutFloat(ptr + 44, d.qW);
	}

}
