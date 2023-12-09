package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.MaterialShaders;

public final class StandardMaterialShaders {
	public static final MaterialShaders DEFAULT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(
			Flywheel.rl("material/default.vert"),
			Flywheel.rl("material/default.frag")));

	private StandardMaterialShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
