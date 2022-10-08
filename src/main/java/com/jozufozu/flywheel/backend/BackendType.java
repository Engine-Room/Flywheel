package com.jozufozu.flywheel.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;

import net.minecraft.network.chat.Component;

public interface BackendType {

	String getProperName();

	String getShortName();

	Component getEngineMessage();

	Engine createEngine();

	BackendType findFallback();

	boolean supported();

	@Nullable SimplePipeline pipelineShader();
}
