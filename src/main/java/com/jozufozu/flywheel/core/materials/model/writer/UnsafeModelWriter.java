package com.jozufozu.flywheel.core.materials.model.writer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.backend.struct.BufferWriterUnsafe;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.backend.struct.BufferWriter;
import com.jozufozu.flywheel.core.materials.model.ModelData;

public class UnsafeModelWriter extends BufferWriterUnsafe<ModelData> {

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

		float[] matrices = d.matrices;
		for (int i = 0; i < matrices.length; i++) {
			MemoryUtil.memPutFloat(addr + i * 4L, matrices[i]);
		}

		advance();
	}
}
