package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.materials.UnsafeBasicWriter;
import com.jozufozu.flywheel.util.WriteUnsafe;

public class UnsafeModelWriter extends UnsafeBasicWriter<ModelData> {

	public UnsafeModelWriter(VecBuffer backingBuffer, StructType<ModelData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(ModelData d) {
		super.writeInternal(d);
		long addr = writePointer + 6;

		((WriteUnsafe) (Object) d.model).writeUnsafe(addr);
		addr += 4 * 16;
		((WriteUnsafe) (Object) d.normal).writeUnsafe(addr);
	}
}
