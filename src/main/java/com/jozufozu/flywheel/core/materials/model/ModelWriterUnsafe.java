package com.jozufozu.flywheel.core.materials.model;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.materials.BasicWriterUnsafe;
import com.jozufozu.flywheel.util.MatrixWrite;

public class ModelWriterUnsafe extends BasicWriterUnsafe<ModelData> {

	public ModelWriterUnsafe(VecBuffer backingBuffer, StructType<ModelData> vertexType) {
		super(backingBuffer, vertexType);
	}

	@Override
	protected void writeInternal(ModelData d) {
		super.writeInternal(d);
		long ptr = writePointer + 6;

		MatrixWrite.writeUnsafe(d.model, ptr);
		MatrixWrite.writeUnsafe(d.normal, ptr + 4 * 16);
	}
}
