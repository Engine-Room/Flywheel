package dev.engine_room.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.MaterialShaders;

public final class StandardMaterialShaders {
	public static final MaterialShaders DEFAULT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(
			Flywheel.rl("material/default.vert"),
			Flywheel.rl("material/default.frag")));

	public static final MaterialShaders WIREFRAME = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Flywheel.rl("material/wireframe.vert"), Flywheel.rl("material/wireframe.frag")));

	public static final MaterialShaders LINE = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Flywheel.rl("material/lines.vert"), Flywheel.rl("material/lines.frag")));

	private StandardMaterialShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}
}
