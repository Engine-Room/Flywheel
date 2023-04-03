package com.jozufozu.flywheel.api.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.registry.IdRegistry;
import com.jozufozu.flywheel.impl.IdRegistryImpl;

import net.minecraft.network.chat.Component;

public interface Backend {
	static IdRegistry<Backend> REGISTRY = IdRegistryImpl.create();

	// TODO: remove and use ID instead? Currently this is only used for the crash log string.
	String getProperName();

	Component getEngineMessage();

	Engine createEngine();

	Backend findFallback();

	boolean isSupported();

	@Nullable Pipeline pipelineShader();
}
