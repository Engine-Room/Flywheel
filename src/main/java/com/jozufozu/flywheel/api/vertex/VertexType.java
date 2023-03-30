package com.jozufozu.flywheel.api.vertex;

import com.jozufozu.flywheel.api.layout.BufferLayout;

import net.minecraft.resources.ResourceLocation;

/**
 * A vertex type containing metadata about a specific vertex layout.
 */
public interface VertexType extends VertexListProvider {

	/**
	 * The layout of this type of vertex when buffered.
	 */
	BufferLayout getLayout();

	ResourceLocation layoutShader();

	default int getStride() {
		return getLayout().getStride();
	}

	default int byteOffset(int vertexIndex) {
		return getStride() * vertexIndex;
	}
}
