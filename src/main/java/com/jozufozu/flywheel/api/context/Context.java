package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public interface Context {
	void onProgramLink(GlProgram program);

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
