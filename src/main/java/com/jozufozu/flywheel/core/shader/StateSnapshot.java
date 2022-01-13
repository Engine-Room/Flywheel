package com.jozufozu.flywheel.core.shader;

import java.util.BitSet;

import com.jozufozu.flywheel.core.GameStateRegistry;

public record StateSnapshot(BitSet ctx) {
	// TODO: is this needed?

	public ShaderConstants getShaderConstants() {
		return GameStateRegistry.getShaderConstants(this);
	}
}
