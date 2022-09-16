package com.jozufozu.flywheel.extension;

import com.jozufozu.flywheel.backend.instancing.batching.DrawBufferSet;

import net.minecraft.client.renderer.RenderType;

/**
 * Duck interface to make RenderType store a DrawBufferSet.
 *
 * @see RenderType
 */
public interface RenderTypeExtension {
	/**
	 * @return The DrawBufferSet associated with this RenderType.
	 */
	DrawBufferSet flywheel$getDrawBufferSet();

	/**
	 * Helper function to cast a RenderType to a RenderTypeExtension and get its DrawBufferSet.
	 * @param type The RenderType to get the DrawBufferSet from.
	 * @return The DrawBufferSet associated with the given RenderType.
	 */
	static DrawBufferSet getDrawBufferSet(RenderType type) {
		return ((RenderTypeExtension) type).flywheel$getDrawBufferSet();
	}
}
