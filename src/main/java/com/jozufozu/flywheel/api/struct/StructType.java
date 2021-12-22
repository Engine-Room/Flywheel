package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.core.layout.BufferLayout;

/**
 * A StructType contains metadata for a specific instance struct that Flywheel can interface with.
 * @param <S> The java representation of the instance struct.
 */
public interface StructType<S> {

	/**
	 * @return A new, zeroed instance of S.
	 */
	S create();

	/**
	 * @return The layout of S when buffered.
	 */
	BufferLayout getLayout();

}
