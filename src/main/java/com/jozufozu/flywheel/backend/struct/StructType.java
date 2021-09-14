package com.jozufozu.flywheel.backend.struct;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;

/**
 * A StructType contains metadata for a specific instance struct that Flywheel can interface with.
 * @param <S> The java representation of the instance struct.
 */
public interface StructType<S> {

	/**
	 * @return A new, zeroed instance of the struct.
	 */
	S create();

	/**
	 * @return The format descriptor of the struct type.
	 */
	VertexFormat format();

	/**
	 * Create a {@link StructWriter} that will consume instances of S and write them to the given buffer.
	 *
	 * @param backing The buffer that the StructWriter will write to.
	 */
	StructWriter<S> getWriter(VecBuffer backing);
}
