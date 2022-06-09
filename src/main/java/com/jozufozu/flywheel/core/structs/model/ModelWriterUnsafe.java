package com.jozufozu.flywheel.core.structs.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.structs.BasicWriterUnsafe;
import com.jozufozu.flywheel.util.MatrixWrite;

public class ModelWriterUnsafe extends BasicWriterUnsafe<ModelData> {

	public ModelWriterUnsafe(StructType<ModelData> structType, ByteBuffer byteBuffer) {
		super(structType, byteBuffer);
	}

	@Override
	protected void writeInternal(ModelData d) {
		super.writeInternal(d);
		long ptr = writePointer + 6;

		((MatrixWrite) (Object) d.model).flywheel$writeUnsafe(ptr);
		((MatrixWrite) (Object) d.normal).flywheel$writeUnsafe(ptr + 4 * 16);
	}
}
