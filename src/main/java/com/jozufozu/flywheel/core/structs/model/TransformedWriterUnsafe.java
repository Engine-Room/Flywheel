package com.jozufozu.flywheel.core.structs.model;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.structs.ColoredLitWriterUnsafe;
import com.jozufozu.flywheel.util.MatrixWrite;

public class TransformedWriterUnsafe extends ColoredLitWriterUnsafe<TransformedPart> {

	public TransformedWriterUnsafe(StructType<TransformedPart> structType, ByteBuffer byteBuffer) {
		super(structType, byteBuffer);
	}

	@Override
	protected void writeInternal(TransformedPart d) {
		super.writeInternal(d);
		long ptr = writePointer + 6;

		((MatrixWrite) (Object) d.model).flywheel$writeUnsafe(ptr);
		((MatrixWrite) (Object) d.normal).flywheel$writeUnsafe(ptr + 4 * 16);
	}
}
