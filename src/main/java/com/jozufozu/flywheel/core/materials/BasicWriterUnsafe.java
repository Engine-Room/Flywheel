package com.jozufozu.flywheel.core.materials;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;

public abstract class BasicWriterUnsafe<D extends BasicData> extends UnsafeBufferWriter<D> {

	public BasicWriterUnsafe(VecBuffer backingBuffer, StructType<D> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(D d) {
		long ptr = writePointer;
		MemoryUtil.memPutByte(ptr, (byte) (d.blockLight << 4));
		MemoryUtil.memPutByte(ptr + 1, (byte) (d.skyLight << 4));
		MemoryUtil.memPutByte(ptr + 2, d.r);
		MemoryUtil.memPutByte(ptr + 3, d.g);
		MemoryUtil.memPutByte(ptr + 4, d.b);
		MemoryUtil.memPutByte(ptr + 5, d.a);
	}
}
