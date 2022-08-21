package com.jozufozu.flywheel.core.structs.oriented;


import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StorageBufferWriter;

public class OrientedStorageWriter implements StorageBufferWriter<OrientedPart> {

	public static final OrientedStorageWriter INSTANCE = new OrientedStorageWriter();

	private OrientedStorageWriter() {
	}

	@Override
	public void write(final long ptr, OrientedPart d) {
		MemoryUtil.memPutFloat(ptr, d.qX);
		MemoryUtil.memPutFloat(ptr + 4, d.qY);
		MemoryUtil.memPutFloat(ptr + 8, d.qZ);
		MemoryUtil.memPutFloat(ptr + 12, d.qW);

		MemoryUtil.memPutFloat(ptr + 16, d.posX);
		MemoryUtil.memPutFloat(ptr + 20, d.posY);
		MemoryUtil.memPutFloat(ptr + 24, d.posZ);

		MemoryUtil.memPutFloat(ptr + 28, d.pivotX);
		MemoryUtil.memPutFloat(ptr + 32, d.pivotY);
		MemoryUtil.memPutFloat(ptr + 36, d.pivotZ);

		MemoryUtil.memPutShort(ptr + 40, d.skyLight);
		MemoryUtil.memPutShort(ptr + 42, d.blockLight);

		MemoryUtil.memPutByte(ptr + 44, d.r);
		MemoryUtil.memPutByte(ptr + 45, d.g);
		MemoryUtil.memPutByte(ptr + 46, d.b);
		MemoryUtil.memPutByte(ptr + 47, d.a);
	}

	@Override
	public int getAlignment() {
		return 48;
	}
}
