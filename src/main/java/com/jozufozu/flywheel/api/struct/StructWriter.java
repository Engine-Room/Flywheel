package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.instancer.InstancedPart;

/**
 * StructWriters can quickly consume many instances of S and write them to some backing buffer.
 */
public interface StructWriter<S extends InstancedPart> {
	/**
	 * Write the given struct to given memory address.
	 */
	void write(long ptr, S struct);
}
