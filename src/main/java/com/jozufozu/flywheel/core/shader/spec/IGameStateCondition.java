package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.util.ResourceLocation;

public interface IGameStateCondition {

	ResourceLocation getID();

	IGameStateProvider getStateProvider();

	boolean isMet();
}
