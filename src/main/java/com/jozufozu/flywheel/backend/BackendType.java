package com.jozufozu.flywheel.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.backend.instancing.Engine;

import net.minecraft.network.chat.Component;

public interface BackendType {

	String getProperName();

	String getShortName();

	Component getEngineMessage();

	Engine createEngine();

	BackendType findFallback();

	boolean supported();

	@Nullable PipelineShader pipelineShader();
}
