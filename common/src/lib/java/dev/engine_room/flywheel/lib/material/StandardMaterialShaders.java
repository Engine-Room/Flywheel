package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.material.MaterialShaders;

public final class StandardMaterialShaders {
	public static final MaterialShaders DEFAULT = new SimpleMaterialShaders(
			Flywheel.rl("material/default.vert"), Flywheel.rl("material/default.frag"));

	public static final MaterialShaders WIREFRAME = new SimpleMaterialShaders(Flywheel.rl("material/wireframe.vert"), Flywheel.rl("material/wireframe.frag"));

	public static final MaterialShaders LINE = new SimpleMaterialShaders(Flywheel.rl("material/lines.vert"), Flywheel.rl("material/lines.frag"));

	public static final MaterialShaders GLINT = new SimpleMaterialShaders(Flywheel.rl("material/glint.vert"), Flywheel.rl("material/default.frag"));

	private StandardMaterialShaders() {
	}
}
