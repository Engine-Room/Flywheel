package com.jozufozu.flywheel.core.materials.model.writer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.UnsafeBufferWriter;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.WriteUnsafe;

public class UnsafeModelWriter extends UnsafeBufferWriter<ModelData> {

	public UnsafeModelWriter(VecBuffer backingBuffer, StructType<ModelData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	public void write(ModelData d) {
		long addr = writePointer;
		MemoryUtil.memPutByte(addr, d.blockLight);
		MemoryUtil.memPutByte(addr + 1, d.skyLight);
		MemoryUtil.memPutByte(addr + 2, d.r);
		MemoryUtil.memPutByte(addr + 3, d.g);
		MemoryUtil.memPutByte(addr + 4, d.b);
		MemoryUtil.memPutByte(addr + 5, d.a);

		addr += 6;

		((WriteUnsafe) (Object) d.model).writeUnsafe(addr);
		addr += 4 * 16;
		((WriteUnsafe) (Object) d.normal).writeUnsafe(addr);

		advance();
	}
}
