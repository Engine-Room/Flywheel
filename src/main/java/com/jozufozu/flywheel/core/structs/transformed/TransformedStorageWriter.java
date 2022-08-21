package com.jozufozu.flywheel.core.structs.transformed;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StorageBufferWriter;
import com.jozufozu.flywheel.util.extension.MatrixExtension;

public class TransformedStorageWriter implements StorageBufferWriter<TransformedPart> {

	public static final TransformedStorageWriter INSTANCE = new TransformedStorageWriter();

	private TransformedStorageWriter() {
	}

	@Override
	public void write(long ptr, TransformedPart instance) {
		MatrixExtension.writeUnsafe(instance.model, ptr);
		MatrixExtension.writeUnsafe(instance.normal, ptr + 64);
		MemoryUtil.memPutByte(ptr + 100, instance.r);
		MemoryUtil.memPutByte(ptr + 101, instance.g);
		MemoryUtil.memPutByte(ptr + 102, instance.b);
		MemoryUtil.memPutByte(ptr + 103, instance.a);
		MemoryUtil.memPutShort(ptr + 104, instance.skyLight);
		MemoryUtil.memPutShort(ptr + 106, instance.blockLight);
	}

	@Override
	public int getAlignment() {
		return 108;
	}
}
