package com.jozufozu.flywheel.api.instance;

/**
 * InstanceWriters can quickly consume many instances and write them to some memory address.
 */
public interface InstanceWriter<I extends Instance> {
	/**
	 * Write the given instance to the given memory address.
	 */
	void write(final long ptr, final I instance);
}
