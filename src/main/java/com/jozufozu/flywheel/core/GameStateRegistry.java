package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.jozufozu.flywheel.core.shader.GameStateProvider;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;

public class GameStateRegistry {

	private static final List<GameStateProvider> PROVIDERS = new ArrayList<>();

	/**
	 * Registers a game state provider.
	 * @param provider The provider to register.
	 */
	public static void register(GameStateProvider provider) {
		PROVIDERS.add(provider);
	}

	/**
	 * Takes a snapshot of the current game state, storing it in a bit set.
	 * @return An object that represents the current game state.
	 */
	public static StateSnapshot takeSnapshot() {
		BitSet bitSet = new BitSet(PROVIDERS.size());

		for (int i = 0, listSize = PROVIDERS.size(); i < listSize; i++) {
			if (PROVIDERS.get(i).isTrue()) {
				bitSet.set(i);
			}
		}
		return new StateSnapshot(bitSet);
	}

	/**
	 * Based on the given snapshot, gathers shader constants to be injected during shader compilation.
	 * @param snapshot The snapshot to use.
	 * @return A list of shader constants.
	 */
	public static ShaderConstants getShaderConstants(StateSnapshot snapshot) {
		BitSet ctx = snapshot.ctx();
		ShaderConstants shaderConstants = new ShaderConstants();

		for (int i = 0, listSize = PROVIDERS.size(); i < listSize; i++) {
			if (ctx.get(i)) {
				PROVIDERS.get(i).alterConstants(shaderConstants);
			}
		}
		return shaderConstants;
	}

	public static void _clear() {
		PROVIDERS.clear();
	}
}
