package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.backend.GameStateRegistry;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

public interface GameStateProvider {

	Codec<GameStateProvider> CODEC = ResourceLocation.CODEC.xmap(GameStateRegistry::getStateProvider, GameStateProvider::getID);

	ResourceLocation getID();

	boolean isTrue();
}
