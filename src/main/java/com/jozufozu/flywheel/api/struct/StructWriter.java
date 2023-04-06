package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.instancer.InstancePart;

/**
 * StructWriters can quickly consume many instances of S and write them to some memory address.
 */
public interface StructWriter<P extends InstancePart> {
	/**
	 * Write the given struct to the given memory address.
	 */
	void write(final long ptr, final P struct);

}
