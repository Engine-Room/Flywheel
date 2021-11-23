package com.jozufozu.flywheel.util;

public interface WriteUnsafe {

	/**
	 * Write the contents of this object into sequential memory starting at the given address.
	 */
	void writeUnsafe(long addr);
}
