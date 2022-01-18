package com.jozufozu.flywheel.core.shader;

/**
 * An object that provides a view of the current game state for shader compilation.
 */
public interface GameStateProvider {

	/**
	 * Get the status of this game state provider.
	 * @return Returning {@code true} will cause #alterConstants to be called before compiling a shader.
	 */
	boolean isTrue();

	/**
	 * Alter the constants for shader compilation.
	 * @param constants The shader constants.
	 */
	void alterConstants(ShaderConstants constants);
}
