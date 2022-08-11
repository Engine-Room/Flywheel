package com.jozufozu.flywheel.core.structs;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructWriter;

public abstract class ColoredLitWriter<D extends ColoredLitPart> implements StructWriter<D> {
	@Override
	public void write(long ptr, D d) {
		MemoryUtil.memPutByte(ptr, d.blockLight);
		MemoryUtil.memPutByte(ptr + 1, d.skyLight);
		MemoryUtil.memPutByte(ptr + 2, d.r);
		MemoryUtil.memPutByte(ptr + 3, d.g);
		MemoryUtil.memPutByte(ptr + 4, d.b);
		MemoryUtil.memPutByte(ptr + 5, d.a);
	}
}
