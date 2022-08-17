package com.jozufozu.flywheel.extension;

import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Duck interface to make VertexFormat store a VertexListProvider.
 *
 * @see VertexFormat
 */
public interface VertexFormatExtension {

	/**
	 * @return The VertexListProvider associated with this VertexFormat.
	 */
	VertexListProvider flywheel$getVertexListProvider();

	void flywheel$setVertexListProvider(VertexListProvider provider);
}
