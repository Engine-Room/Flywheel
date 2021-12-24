package com.jozufozu.flywheel.util;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

/**
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix3fMixin
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix4fMixin
 */
public interface MatrixWrite {

	/**
	 * Write the contents of this object into sequential memory starting at the given address.
	 */
	void flywheel$writeUnsafe(long ptr);

	void flywheel$write(VecBuffer buf);
}
