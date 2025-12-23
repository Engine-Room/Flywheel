package dev.engine_room.flywheel.api.material;

import net.minecraft.resources.Identifier;

/**
 * A shader that decides what colors should be discarded in the fragment shader.
 */
public interface CutoutShader {
	/**
	 * @apiNote {@code flywheel/} is implicitly prepended to the {@link Identifier}'s path.
	 */
	Identifier source();
}
