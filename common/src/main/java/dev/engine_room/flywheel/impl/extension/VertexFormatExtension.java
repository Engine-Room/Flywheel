package dev.engine_room.flywheel.impl.extension;

import dev.engine_room.flywheel.api.vertex.VertexViewProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Duck interface to make VertexFormat store a VertexListProvider.
 *
 * @see VertexFormat
 */
public interface VertexFormatExtension {
	/**
	 * @return The VertexViewProvider associated with this VertexFormat.
	 */
	VertexViewProvider flywheel$getVertexViewProvider();

	void flywheel$setVertexViewProvider(VertexViewProvider provider);
}
