package com.jozufozu.flywheel.core.materials.oriented;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.materials.BasicWriterUnsafe;

public class OrientedWriterUnsafe extends BasicWriterUnsafe<OrientedData> {
	public OrientedWriterUnsafe(VecBuffer backingBuffer, StructType<OrientedData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(OrientedData d) {
		long addr = writePointer;
		super.writeInternal(d);

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
	}
}
