package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.source.FileResolution;

/**
 * A vertex type containing metadata about a specific vertex layout.
 */
public interface VertexType extends VertexListProvider {

	/**
	 * The layout of this type of vertex when buffered.
	 */
	BufferLayout getLayout();

	FileResolution getLayoutShader();

	default int getStride() {
		return getLayout().getStride();
	}

	default int byteOffset(int vertexIndex) {
		return getStride() * vertexIndex;
	}
}
