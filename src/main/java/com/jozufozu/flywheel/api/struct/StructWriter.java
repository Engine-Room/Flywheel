package com.jozufozu.flywheel.api.struct;

/**
 * StructWriters can quickly consume many instances of S and write them to some backing buffer.
 */
public interface StructWriter<S> {

	/**
	 * Write the given struct to the backing array.
	 */
	void write(S struct);

	/**
	 * Seek to the given position. The next write will occur there.
	 */
	void seek(int pos);
}
