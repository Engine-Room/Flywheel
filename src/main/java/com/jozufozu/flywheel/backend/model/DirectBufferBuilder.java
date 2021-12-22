package com.jozufozu.flywheel.backend.model;

import com.mojang.blaze3d.vertex.BufferBuilder;

/**
 * Duck interface used on {@link BufferBuilder} to provide lower level access to the backing memory.
 */
public interface DirectBufferBuilder {

	/**
	 * Create a DirectVertexConsumer from this BufferBuilder.
	 *
	 * <p>
	 *     After this function returns, the internal state of the BufferBuilder will be as if
	 *     {@link BufferBuilder#endVertex()} was called vertexCount times. It is up to the callee
	 *     to actually populate the BufferBuilder with vertices using the returned value.
	 * </p>
	 */
	DirectVertexConsumer intoDirectConsumer(int vertexCount);
}
