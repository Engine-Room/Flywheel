package dev.engine_room.flywheel.api.backend;

import dev.engine_room.flywheel.api.BackendImplemented;
import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.registry.IdRegistry;
import net.minecraft.world.level.LevelAccessor;

@BackendImplemented
public interface Backend {
	IdRegistry<Backend> REGISTRY = FlwApiLink.INSTANCE.createIdRegistry();

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
