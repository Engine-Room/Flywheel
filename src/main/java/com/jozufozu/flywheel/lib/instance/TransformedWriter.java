package com.jozufozu.flywheel.lib.instance;

import com.jozufozu.flywheel.lib.math.MatrixUtil;

public class TransformedWriter extends ColoredLitWriter<TransformedInstance> {
	public static final TransformedWriter INSTANCE = new TransformedWriter();

	@Override
	public void write(final long ptr, final TransformedInstance instance) {
		super.write(ptr, instance);
		MatrixUtil.writeUnsafe(instance.model, ptr + 8);
		MatrixUtil.writeUnsafe(instance.normal, ptr + 72);
	}

}
