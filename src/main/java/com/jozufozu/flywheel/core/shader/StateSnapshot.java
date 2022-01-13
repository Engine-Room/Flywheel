package com.jozufozu.flywheel.core.shader;

import com.jozufozu.flywheel.core.GameStateRegistry;

public record StateSnapshot(long ctx) {
	// TODO: is this needed?

	public ShaderConstants getDefines() {
		return GameStateRegistry.getDefines(ctx);
	}
}
