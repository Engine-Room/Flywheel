package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public interface Context {
	static Registry<Context> REGISTRY = InternalFlywheelApi.INSTANCE.createRegistry();

	void onProgramLink(GlProgram program);

	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();
}
