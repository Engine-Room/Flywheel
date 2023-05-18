package com.jozufozu.flywheel.api.context;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.resources.ResourceLocation;

public interface Context {
	static Registry<Context> REGISTRY = RegistryImpl.create();

	void onProgramLink(@NotNull GlProgram program);

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
