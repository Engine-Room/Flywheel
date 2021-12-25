package com.jozufozu.flywheel.backend.struct;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

/**
 * This class copied/adapted from jellysquid's
 *
 * An unsafe {@link BufferWriter} implementation which uses direct memory operations to enable fast blitting of
 * data into memory buffers. Only available on JVMs which support {@link sun.misc.Unsafe}, but generally produces much
 * better optimized code than other implementations. The implementation does not check for invalid memory accesses,
 * meaning that errors can corrupt process memory.
 */
public abstract class UnsafeBufferWriter<S> extends BufferWriter<S> {
	/**
	 * The write pointer into the buffer storage. This is advanced by the stride every time
	 * {@link UnsafeBufferWriter#advance()} is called.
	 */
	protected long writePointer;

	protected UnsafeBufferWriter(VecBuffer backingBuffer, StructType<S> vertexType) {
		super(backingBuffer, vertexType);

		acquireWritePointer();
	}

	@Override
	public void seek(int pos) {
		super.seek(pos);
		acquireWritePointer();
	}

	@Override
	protected void advance() {
		this.writePointer += this.stride;
	}

	private void acquireWritePointer() {
		this.writePointer = MemoryUtil.memAddress(this.backingBuffer.unwrap(), this.backingBuffer.position());
	}
}
