package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.Identifier;

/**
 * A shader that controls the fog effect on a material.
 */
public interface FogShader {
	/**
	 * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
	 */
	Identifier source();
}
