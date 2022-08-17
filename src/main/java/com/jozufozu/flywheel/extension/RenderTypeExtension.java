package com.jozufozu.flywheel.extension;

import com.jozufozu.flywheel.backend.instancing.batching.DrawBuffer;

import net.minecraft.client.renderer.RenderType;

/**
 * Duck interface to make RenderType store a DrawBuffer.
 *
 * @see RenderType
 */
public interface RenderTypeExtension {

	/**
	 * @return The DrawBuffer associated with this RenderType.
	 */
	DrawBuffer flywheel$getDrawBuffer();

	/**
	 * Helper function to cast a RenderType to a RenderTypeExtension and get its DrawBuffer.
	 * @param type The RenderType to get the DrawBuffer from.
	 * @return The DrawBuffer associated with the given RenderType.
	 */
	static DrawBuffer getDrawBuffer(RenderType type) {
		return ((RenderTypeExtension) type).flywheel$getDrawBuffer();
	}
}
