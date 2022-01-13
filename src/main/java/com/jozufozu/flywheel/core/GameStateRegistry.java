package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.shader.GameStateProvider;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;

import net.minecraft.resources.ResourceLocation;

public class GameStateRegistry {

	private static final Map<ResourceLocation, GameStateProvider> registeredStateProviders = new HashMap<>();

	public static void _clear() {
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

	public static StateSnapshot takeSnapshot() {
		long ctx = 0;
		for (GameStateProvider state : registeredStateProviders.values()) {
			if (state.isTrue()) {
				ctx |= 1;
			}
			ctx <<= 1;
		}
		return new StateSnapshot(ctx);
	}

	public static ShaderConstants getDefines(long ctx) {
		long stateID = ctx;
		ShaderConstants shaderConstants = new ShaderConstants();

		for (GameStateProvider state : registeredStateProviders.values()) {
			if ((stateID & 1) == 1) {
				state.alterConstants(shaderConstants);
			}
			stateID >>= 1;
		}
		return shaderConstants;
	}
}
