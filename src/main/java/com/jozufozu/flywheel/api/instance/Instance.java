package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.util.joml.FrustumIntersection;

import net.minecraft.core.BlockPos;

public interface Instance {
    BlockPos getWorldPosition();

	/**
	 * Check this instance against a frustum.<p>
	 * An implementor may choose to return a constant to skip the frustum check.
	 * @param frustum A frustum intersection tester for the current frame.
	 * @return {@code true} if this instance should be considered for updates.
	 */
	boolean checkFrustum(FrustumIntersection frustum);

	boolean isRemoved();
}
