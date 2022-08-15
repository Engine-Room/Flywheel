package com.jozufozu.flywheel.core.structs.oriented;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.core.structs.ColoredLitWriter;

public class OrientedWriter extends ColoredLitWriter<OrientedPart> {
	public static final OrientedWriter INSTANCE = new OrientedWriter();

	@Override
	public void write(long ptr, OrientedPart d) {
		super.write(ptr, d);

		MemoryUtil.memPutFloat(ptr + 6, d.posX);
		MemoryUtil.memPutFloat(ptr + 10, d.posY);
		MemoryUtil.memPutFloat(ptr + 14, d.posZ);
		MemoryUtil.memPutFloat(ptr + 18, d.pivotX);
		MemoryUtil.memPutFloat(ptr + 22, d.pivotY);
		MemoryUtil.memPutFloat(ptr + 26, d.pivotZ);
		MemoryUtil.memPutFloat(ptr + 30, d.qX);
		MemoryUtil.memPutFloat(ptr + 34, d.qY);
		MemoryUtil.memPutFloat(ptr + 38, d.qZ);
		MemoryUtil.memPutFloat(ptr + 42, d.qW);
	}
}
