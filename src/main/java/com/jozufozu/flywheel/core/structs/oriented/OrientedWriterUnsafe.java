package com.jozufozu.flywheel.core.structs.oriented;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.structs.BasicWriterUnsafe;

public class OrientedWriterUnsafe extends BasicWriterUnsafe<OrientedData> {
	public OrientedWriterUnsafe(StructType<OrientedData> structType, ByteBuffer byteBuffer) {
		super(structType, byteBuffer);
	}

	@Override
	protected void writeInternal(OrientedData d) {
		long ptr = writePointer;
		super.writeInternal(d);

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
