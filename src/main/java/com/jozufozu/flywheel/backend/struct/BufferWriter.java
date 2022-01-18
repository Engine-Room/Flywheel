package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.struct.StructWriter;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public abstract class BufferWriter<S> implements StructWriter<S> {
	protected final VecBuffer backingBuffer;

	protected final int stride;

	protected BufferWriter(VecBuffer backingBuffer, StructType<S> vertexType) {
		this.backingBuffer = backingBuffer;

		this.stride = vertexType.getLayout().getStride();
	}

	@Override
	public final void write(S struct) {
		writeInternal(struct);
		advance();
	}

	/**
	 * Advances the write pointer forward by the stride of one vertex.
	 * This will always be called after a struct is written, implementors need not call it themselves.
	 *
	 * @see #write
	 */
	protected abstract void advance();

	protected abstract void writeInternal(S s);

	@Override
	public void seek(int pos) {
		backingBuffer.position(pos * stride);
	}
}
