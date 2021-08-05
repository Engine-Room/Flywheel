package com.jozufozu.flywheel.core;

import net.minecraft.client.renderer.culling.Frustum;

/**
 * Used to capture the Frustum from WorldRenderer#renderLevel
 */
public class Clipping {

	/**
	 * Assigned in {@link com.jozufozu.flywheel.mixin.GlobalClippingHelperMixin this} mixin.
	 */
	public static Frustum HELPER;
}
