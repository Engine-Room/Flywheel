package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.core.compile.ShaderConstants;

import net.minecraft.resources.ResourceLocation;

public interface GameStateProvider {

	ResourceLocation getID();

	boolean isTrue();

	void alterConstants(ShaderConstants constants);
}
