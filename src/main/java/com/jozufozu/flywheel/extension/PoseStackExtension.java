package com.jozufozu.flywheel.extension;

import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * An extension interface for {@link PoseStack} that provides a {@link PoseTransformStack} wrapper.
 * <br>
 * Each PoseStack lazily creates and saves a wrapper instance. This wrapper is cached and reused for all future calls.
 */
public interface PoseStackExtension {
	/**
	 * @return The {@link PoseTransformStack} wrapper for this {@link PoseStack}.
	 */
	PoseTransformStack flywheel$transformStack();
}
