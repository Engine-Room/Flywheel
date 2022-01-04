package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.GameStateProvider;

import net.minecraft.resources.ResourceLocation;

public interface GameStateCondition {

	ResourceLocation getID();

	GameStateProvider getStateProvider();

	boolean isMet();
}
