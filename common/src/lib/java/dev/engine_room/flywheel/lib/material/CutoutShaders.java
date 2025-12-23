package dev.engine_room.flywheel.lib.material;

import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

public final class CutoutShaders {
	/**
	 * Do not discard any fragments based on alpha.
	 */
	public static final CutoutShader OFF = new SimpleCutoutShader(IdentifierUtil.id("cutout/off.glsl"));
	/**
	 * Discard fragments with alpha close to or equal to zero.
	 */
	public static final CutoutShader EPSILON = new SimpleCutoutShader(IdentifierUtil.id("cutout/epsilon.glsl"));
	/**
	 * Discard fragments with alpha less than to 0.1.
	 */
	public static final CutoutShader ONE_TENTH = new SimpleCutoutShader(IdentifierUtil.id("cutout/one_tenth.glsl"));
	/**
	 * Discard fragments with alpha less than to 0.5.
	 */
	public static final CutoutShader HALF = new SimpleCutoutShader(IdentifierUtil.id("cutout/half.glsl"));

	private CutoutShaders() {
	}
}
