package com.jozufozu.flywheel.core.structs.transformed;

import com.jozufozu.flywheel.core.structs.ColoredLitWriter;
import com.jozufozu.flywheel.util.extension.MatrixExtension;

public class TransformedWriter extends ColoredLitWriter<TransformedPart> {
	public static final TransformedWriter INSTANCE = new TransformedWriter();

	@Override
	public void write(long ptr, TransformedPart d) {
		super.write(ptr, d);
		ptr += 6;

		((MatrixExtension) (Object) d.model).flywheel$writeUnsafe(ptr);
		((MatrixExtension) (Object) d.normal).flywheel$writeUnsafe(ptr + 4 * 16);
	}
}
