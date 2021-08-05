package com.jozufozu.flywheel.core.shader.spec;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.resources.ResourceLocation;

public interface IContextCondition {

	ResourceLocation getID();

	IGameStateProvider contextProvider();

	boolean get();
}
