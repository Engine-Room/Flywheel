package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.resources.ResourceLocation;

public interface IGameStateCondition {

	ResourceLocation getID();

	IGameStateProvider getStateProvider();

	boolean isMet();
}
