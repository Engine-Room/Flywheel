package com.jozufozu.flywheel.backend.gl.buffer;

/**
 * Interface for generically dealing with mapped buffers.
 */
public interface Mappable {
	GlBufferType getType();

	/**
	 * Indicates that this buffer need not be #flush()'d for its contents to sync.
	 * @return true if this buffer is persistently mapped.
	 */
	boolean isPersistent();
}
