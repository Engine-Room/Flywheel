package com.jozufozu.flywheel.api.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.registry.IdRegistry;
import com.jozufozu.flywheel.impl.IdRegistryImpl;

import net.minecraft.network.chat.Component;

public interface Backend {
	static IdRegistry<Backend> REGISTRY = IdRegistryImpl.create();

	/**
	 * Get a message to display to the user when the engine is enabled.
	 */
	Component engineMessage();

	/**
	 * Create a new engine instance.
	 */
	Engine createEngine();

	/**
	 * Get a fallback backend in case this backend is not supported.
	 */
	Backend findFallback();

	/**
	 * Check if this backend is supported.
	 */
	boolean isSupported();

	@Nullable Pipeline pipelineShader();
}
