package com.jozufozu.flywheel.core.materials.oriented.writer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;

public class UnsafeOrientedWriter extends UnsafeBufferWriter<OrientedData> {
	public UnsafeOrientedWriter(VecBuffer backingBuffer, StructType<OrientedData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	public void write(OrientedData d) {
		long addr = writePointer;
		MemoryUtil.memPutByte(addr, d.blockLight);
		MemoryUtil.memPutByte(addr + 1, d.skyLight);
		MemoryUtil.memPutByte(addr + 2, d.r);
		MemoryUtil.memPutByte(addr + 3, d.g);
		MemoryUtil.memPutByte(addr + 4, d.b);
		MemoryUtil.memPutByte(addr + 5, d.a);

		MemoryUtil.memPutFloat(addr + 6, d.posX);
		MemoryUtil.memPutFloat(addr + 10, d.posY);
		MemoryUtil.memPutFloat(addr + 14, d.posZ);
		MemoryUtil.memPutFloat(addr + 18, d.pivotX);
		MemoryUtil.memPutFloat(addr + 22, d.pivotY);
		MemoryUtil.memPutFloat(addr + 26, d.pivotZ);
		MemoryUtil.memPutFloat(addr + 30, d.qX);
		MemoryUtil.memPutFloat(addr + 34, d.qY);
		MemoryUtil.memPutFloat(addr + 38, d.qZ);
		MemoryUtil.memPutFloat(addr + 42, d.qW);

		advance();
	}
}
