package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.api.vertex.VertexType;

public interface VertexData {
	/**
	 * Generate the necessary glue code here.
	 * @param file The SourceFile with user written code.
	 */
	String generateFooter(FileIndex file, VertexType vertexType);
}
