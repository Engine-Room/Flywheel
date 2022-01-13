package com.jozufozu.flywheel.core.shader;

import net.minecraft.resources.ResourceLocation;

public interface GameStateProvider {

	ResourceLocation getID();

	boolean isTrue();

	void alterConstants(ShaderConstants constants);
}
