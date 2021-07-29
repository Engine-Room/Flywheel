package com.jozufozu.flywheel.core;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.culling.ClippingHelper;

/**
 * Used to capture the ClippingHelper from WorldRenderer#renderLevel
 */
public class Clipping {

	/**
	 * Assigned in {@link com.jozufozu.flywheel.mixin.GlobalClippingHelperMixin this} mixin.
	 */
	public static ClippingHelper HELPER;
}
