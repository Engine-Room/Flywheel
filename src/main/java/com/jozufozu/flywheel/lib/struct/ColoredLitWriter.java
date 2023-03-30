package com.jozufozu.flywheel.lib.struct;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructWriter;

public abstract class ColoredLitWriter<D extends ColoredLitPart> implements StructWriter<D> {
	@Override
	public void write(final long ptr, final D d) {
		MemoryUtil.memPutShort(ptr, d.blockLight);
		MemoryUtil.memPutShort(ptr + 2, d.skyLight);
		MemoryUtil.memPutByte(ptr + 4, d.r);
		MemoryUtil.memPutByte(ptr + 5, d.g);
		MemoryUtil.memPutByte(ptr + 6, d.b);
		MemoryUtil.memPutByte(ptr + 7, d.a);
	}
}
