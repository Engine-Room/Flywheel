package com.jozufozu.flywheel.api.backend;

import com.jozufozu.flywheel.api.BackendImplemented;
import com.jozufozu.flywheel.api.internal.FlwApiLink;
import com.jozufozu.flywheel.api.registry.IdRegistry;

import net.minecraft.world.level.LevelAccessor;

@BackendImplemented
public interface Backend {
	static IdRegistry<Backend> REGISTRY = FlwApiLink.INSTANCE.createIdRegistry();

	/**
	 * Create a new engine instance.
	 */
	Engine createEngine(LevelAccessor level);

	/**
	 * Get a fallback backend in case this backend is not supported.
	 */
	Backend findFallback();

	/**
	 * Check if this backend is supported.
	 */
	boolean isSupported();
}
