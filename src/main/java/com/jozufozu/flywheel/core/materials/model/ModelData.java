package com.jozufozu.flywheel.core.materials.model;

import java.nio.FloatBuffer;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.materials.BasicData;
import com.jozufozu.flywheel.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;

public class ModelData extends BasicData {
	private static final float[] empty = new float[25];

	public final float[] matrices = empty.clone();
	private final FloatBuffer buf = FloatBuffer.wrap(matrices);

	public ModelData setTransform(PoseStack stack) {
		this.buf.reset();

		stack.last().pose().store(this.buf);
		stack.last().normal().store(this.buf);
		markDirty();
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 *     This will allow the gpu to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public ModelData setEmptyTransform() {
		this.buf.reset();
		this.buf.put(empty);
		markDirty();
		return this;
	}

	@Override
	public void write(VecBuffer buf) {
		super.write(buf);
		buf.put(this.buf);
	}
}
