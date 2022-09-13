package com.jozufozu.flywheel.core.structs.transformed;

import com.jozufozu.flywheel.core.structs.ColoredLitWriter;
import com.jozufozu.flywheel.extension.MatrixWrite;

public class TransformedWriter extends ColoredLitWriter<TransformedPart> {
	public static final TransformedWriter INSTANCE = new TransformedWriter();

	@Override
	public void write(final long ptr, final TransformedPart d) {
		super.write(ptr, d);
		MatrixWrite.writeUnsafe(d.model, ptr + 8);
		MatrixWrite.writeUnsafe(d.normal, ptr + 72);
	}

}
