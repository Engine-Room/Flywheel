package dev.engine_room.flywheel.impl.extension;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.transform.PoseTransformStack;

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
