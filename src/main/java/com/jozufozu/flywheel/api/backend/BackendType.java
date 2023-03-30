package com.jozufozu.flywheel.api.backend;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.pipeline.Pipeline;

import net.minecraft.network.chat.Component;

public interface BackendType {

	String getProperName();

	String getShortName();

	Component getEngineMessage();

	Engine createEngine();

	BackendType findFallback();

	boolean supported();

	@Nullable Pipeline pipelineShader();
}
