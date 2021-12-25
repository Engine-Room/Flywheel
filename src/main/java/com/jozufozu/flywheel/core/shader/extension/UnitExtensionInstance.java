package com.jozufozu.flywheel.core.shader.extension;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public class UnitExtensionInstance implements IExtensionInstance {

	public static final ResourceLocation NAME = Flywheel.rl("unit");

	public UnitExtensionInstance(GlProgram program) {
	}

	@Override
	public void bind() {

	}

	@Override
	public ResourceLocation name() {
		return NAME;
	}
}
