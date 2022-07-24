package com.jozufozu.flywheel.core.structs;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public abstract class ColoredLitWriterUnsafe<D extends ColoredLitPart> extends UnsafeBufferWriter<D> {

	public ColoredLitWriterUnsafe(StructType<D> structType, ByteBuffer byteBuffer) {
		super(structType, byteBuffer);
	}

	@Override
	protected void writeInternal(D d) {
		long ptr = writePointer;
		MemoryUtil.memPutByte(ptr, d.blockLight);
		MemoryUtil.memPutByte(ptr + 1, d.skyLight);
		MemoryUtil.memPutByte(ptr + 2, d.r);
		MemoryUtil.memPutByte(ptr + 3, d.g);
		MemoryUtil.memPutByte(ptr + 4, d.b);
		MemoryUtil.memPutByte(ptr + 5, d.a);
	}
}
