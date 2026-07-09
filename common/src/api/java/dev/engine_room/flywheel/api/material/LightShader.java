package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.Identifier;

/**
 * A shader that controls the GPU-based light on a material.
 */
public interface LightShader {
	/**
	 * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
	 */
	Identifier source();
}
