package com.jozufozu.flywheel.util.extension;

import java.nio.ByteBuffer;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

/**
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix3fMixin
 * @see com.jozufozu.flywheel.mixin.matrix.Matrix4fMixin
 */
public interface MatrixExtension {

	/**
	 * Write the contents of this object into sequential memory starting at the given address.
	 */
	void flywheel$writeUnsafe(long ptr);

	void flywheel$write(ByteBuffer buf);

	static void write(Matrix4f matrix, ByteBuffer buf) {
		((MatrixExtension) (Object) matrix).flywheel$write(buf);
	}

	static void writeUnsafe(Matrix4f matrix, long ptr) {
		((MatrixExtension) (Object) matrix).flywheel$writeUnsafe(ptr);
	}

	static void writeUnsafe(Matrix3f matrix, long ptr) {
		((MatrixExtension) (Object) matrix).flywheel$writeUnsafe(ptr);
	}
}
