package com.jozufozu.flywheel.api.vertex;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.core.layout.BufferLayout;

/**
 * A vertex type containing metadata about a specific vertex layout.
 */
public interface VertexType {

	/**
	 * The layout of this type of vertex when buffered.
	 */
	BufferLayout getLayout();

	/**
	 * Create a writer backed by the given ByteBuffer.
	 *
	 * <p>
	 *     Implementors are encouraged to override the return type for ergonomics.
	 * </p>
	 */
	VertexWriter createWriter(ByteBuffer buffer);

	/**
	 * Create a view of the given ByteBuffer as if it were already filled with vertices.
	 *
	 * <p>
	 *     Implementors are encouraged to override the return type for ergonomics.
	 * </p>
	 */
	VertexList createReader(ByteBuffer buffer, int vertexCount);

	String getShaderHeader();

	default int getStride() {
		return getLayout().getStride();
	}

	default int byteOffset(int vertexIndex) {
		return getStride() * vertexIndex;
	}
}
