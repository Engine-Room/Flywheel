package com.jozufozu.flywheel.lib.instance;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.InstanceWriter;

public abstract class ColoredLitWriter<I extends ColoredLitInstance> implements InstanceWriter<I> {
	@Override
	public void write(final long ptr, final I instance) {
		MemoryUtil.memPutShort(ptr, instance.blockLight);
		MemoryUtil.memPutShort(ptr + 2, instance.skyLight);
		MemoryUtil.memPutByte(ptr + 4, instance.r);
		MemoryUtil.memPutByte(ptr + 5, instance.g);
		MemoryUtil.memPutByte(ptr + 6, instance.b);
		MemoryUtil.memPutByte(ptr + 7, instance.a);
	}
}
