package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.shader.gamestate.GameStateProvider;

import net.minecraft.resources.ResourceLocation;

public class GameStateRegistry {

	private static final Map<ResourceLocation, GameStateProvider> registeredStateProviders = new HashMap<>();

	static void clear() {
		registeredStateProviders.clear();
	}

	public static GameStateProvider getStateProvider(ResourceLocation location) {
		GameStateProvider out = registeredStateProviders.get(location);

		if (out == null) {
			throw new IllegalArgumentException("State provider '" + location + "' does not exist.");
		}

		return out;
	}

	public static void register(GameStateProvider context) {
		if (registeredStateProviders.containsKey(context.getID())) {
			throw new IllegalStateException("Duplicate game state provider: " + context.getID());
		}

		registeredStateProviders.put(context.getID(), context);
	}
}
