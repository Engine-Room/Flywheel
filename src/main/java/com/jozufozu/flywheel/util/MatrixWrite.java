package com.jozufozu.flywheel.util;

import java.nio.ByteBuffer;

/**
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix3fMixin
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix4fMixin
 */
public interface MatrixWrite {

	/**
	 * Write the contents of this object into sequential memory starting at the given address.
	 */
	void flywheel$writeUnsafe(long ptr);

	void flywheel$write(ByteBuffer buf);
}
