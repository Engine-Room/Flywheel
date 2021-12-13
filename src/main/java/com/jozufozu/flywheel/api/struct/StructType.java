package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

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

	Instanced<S> asInstanced();

	Batched<S> asBatched();
}
