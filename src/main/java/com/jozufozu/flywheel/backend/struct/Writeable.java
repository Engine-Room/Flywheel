package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

public interface Writeable<S> extends StructType<S> {
	/**
	 * Create a {@link StructWriter} that will consume instances of S and write them to the given buffer.
	 *
	 * @param backing The buffer that the StructWriter will write to.
	 */
	StructWriter<S> getWriter(VecBuffer backing);

	@Override
	default Writeable<S> asWriteable() {
		return this;
	}
}
