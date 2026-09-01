package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.MaterialShaders;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

public final class StandardMaterialShaders {
	public static final MaterialShaders DEFAULT = new SimpleMaterialShaders(
			IdentifierUtil.id("material/default.vert"), IdentifierUtil.id("material/default.frag"));

	public static final MaterialShaders WIREFRAME = new SimpleMaterialShaders(IdentifierUtil.id("material/wireframe.vert"), IdentifierUtil.id("material/wireframe.frag"));

	public static final MaterialShaders LINE = new SimpleMaterialShaders(IdentifierUtil.id("material/lines.vert"), IdentifierUtil.id("material/lines.frag"));

	public static final MaterialShaders GLINT = new SimpleMaterialShaders(IdentifierUtil.id("material/glint.vert"), IdentifierUtil.id("material/default.frag"));

	private StandardMaterialShaders() {
	}
}
