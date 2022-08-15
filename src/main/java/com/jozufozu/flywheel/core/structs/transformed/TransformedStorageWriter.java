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
		MemoryUtil.memPutByte(ptr + 112, instance.r);
		MemoryUtil.memPutByte(ptr + 113, instance.g);
		MemoryUtil.memPutByte(ptr + 114, instance.b);
		MemoryUtil.memPutByte(ptr + 115, instance.a);
		MemoryUtil.memPutShort(ptr + 116, instance.skyLight);
		MemoryUtil.memPutShort(ptr + 118, instance.blockLight);
	}

	@Override
	public int getAlignment() {
		return 128;
	}
}
