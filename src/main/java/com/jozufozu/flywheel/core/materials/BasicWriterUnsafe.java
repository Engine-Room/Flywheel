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
		long addr = writePointer;
		MemoryUtil.memPutByte(addr, (byte) (d.blockLight << 4));
		MemoryUtil.memPutByte(addr + 1, (byte) (d.skyLight << 4));
		MemoryUtil.memPutByte(addr + 2, d.r);
		MemoryUtil.memPutByte(addr + 3, d.g);
		MemoryUtil.memPutByte(addr + 4, d.b);
		MemoryUtil.memPutByte(addr + 5, d.a);
	}
}
