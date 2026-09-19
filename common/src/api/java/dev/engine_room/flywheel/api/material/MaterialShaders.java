package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.Identifier;

/**
 * A vertex and fragment shader pair that can be attached to a material.
 */
public interface MaterialShaders {
	/**
	 * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
	 */
	Identifier vertexSource();

	/**
	 * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
	 */
	Identifier fragmentSource();
}
