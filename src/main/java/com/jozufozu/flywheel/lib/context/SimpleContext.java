package com.jozufozu.flywheel.lib.context;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public record SimpleContext(ResourceLocation vertexShader, ResourceLocation fragmentShader, Consumer<GlProgram> onLink) implements Context {
	@Override
	public void onProgramLink(GlProgram program) {
		onLink.accept(program);
	}

	@Override
	public ResourceLocation vertexShader() {
		return vertexShader;
	}

	@Override
	public ResourceLocation fragmentShader() {
		return fragmentShader;
	}
}
