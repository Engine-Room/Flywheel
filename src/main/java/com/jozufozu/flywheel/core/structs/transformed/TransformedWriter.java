package com.jozufozu.flywheel.core.structs.transformed;

import com.jozufozu.flywheel.core.structs.ColoredLitWriter;
import com.jozufozu.flywheel.util.MatrixWrite;

public class TransformedWriter extends ColoredLitWriter<TransformedPart> {
	public static final TransformedWriter INSTANCE = new TransformedWriter();

	@Override
	public void write(long ptr, TransformedPart d) {
		super.write(ptr, d);
		ptr += 6;

		((MatrixWrite) (Object) d.model).flywheel$writeUnsafe(ptr);
		((MatrixWrite) (Object) d.normal).flywheel$writeUnsafe(ptr + 4 * 16);
	}
}
