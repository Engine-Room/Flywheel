package com.jozufozu.flywheel.core.shader.gamestate;

import com.jozufozu.flywheel.backend.GameStateRegistry;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

public interface IGameStateProvider {

	Codec<IGameStateProvider> CODEC = ResourceLocation.CODEC.xmap(GameStateRegistry::getStateProvider, IGameStateProvider::getID);

	ResourceLocation getID();

	Object getValue();
}
