package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.lib.math.MatrixMath;

public class TransformedWriter extends ColoredLitWriter<TransformedInstance> {
	public static final TransformedWriter INSTANCE = new TransformedWriter();

	@Override
	public void write(final long ptr, final TransformedInstance instance) {
		super.write(ptr, instance);
		MatrixMath.writeUnsafe(instance.model, ptr + 8);
		MatrixMath.writeUnsafe(instance.normal, ptr + 72);
	}
}
