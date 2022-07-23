package com.jozufozu.flywheel.backend.struct;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;

/**
 * This class copied/adapted from jellysquid's
 *
 * An unsafe {@link BufferWriter} implementation which uses direct memory operations to enable fast blitting of
 * data into memory buffers. Only available on JVMs which support {@link sun.misc.Unsafe}, but generally produces much
 * better optimized code than other implementations. The implementation does not check for invalid memory accesses,
 * meaning that errors can corrupt process memory.
 */
public abstract class UnsafeBufferWriter<S extends InstancedPart> extends BufferWriter<S> {
	/**
	 * The write pointer into the buffer storage. This is advanced by the stride every time
	 * {@link UnsafeBufferWriter#advance()} is called.
	 */
	protected long writePointer;

	protected UnsafeBufferWriter(StructType<S> structType, ByteBuffer byteBuffer) {
		super(structType, byteBuffer);

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
		this.writePointer = MemoryUtil.memAddress(this.backingBuffer, this.backingBuffer.position());
	}
}
