package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public abstract class BufferWriter<S> implements StructWriter<S> {
	protected final VecBuffer backingBuffer;

	protected final VertexFormat format;
	protected final int stride;

	protected BufferWriter(VecBuffer backingBuffer, StructType<S> vertexType) {
		this.backingBuffer = backingBuffer;

		this.format = vertexType.format();
		this.stride = this.format.getStride();
	}

	/**
	 * Advances the write pointer forward by the stride of one vertex. This should always be called after a
	 * vertex is written. Implementations which override this should always call invoke the super implementation.
	 */
	protected void advance() {

	}

	@Override
	public void seek(int pos) {
		backingBuffer.position(pos * stride);
	}
}
