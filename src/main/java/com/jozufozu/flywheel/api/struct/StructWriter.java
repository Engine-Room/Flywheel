package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.instancer.InstancedPart;

/**
 * StructWriters can quickly consume many instances of S and write them to some memory address.
 */
public interface StructWriter<S extends InstancedPart> {
	/**
	 * Write the given struct to the given memory address.
	 */
	void write(final long ptr, final S struct);

}
