package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.shader.gamestate.IGameStateProvider;

import net.minecraft.resources.ResourceLocation;

public class GameStateRegistry {

	private static final Map<ResourceLocation, IGameStateProvider> registeredStateProviders = new HashMap<>();

	static void clear() {
		registeredStateProviders.clear();
	}

	public static IGameStateProvider getStateProvider(ResourceLocation location) {
		IGameStateProvider out = registeredStateProviders.get(location);

		if (out == null) {
			throw new IllegalArgumentException("State provider '" + location + "' does not exist.");
		}

		return out;
	}

	public static void register(IGameStateProvider context) {
		if (registeredStateProviders.containsKey(context.getID())) {
			throw new IllegalStateException("Duplicate game state provider: " + context.getID());
		}

		registeredStateProviders.put(context.getID(), context);
	}
}
