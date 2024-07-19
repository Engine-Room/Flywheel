package dev.engine_room.flywheel.backend.engine.embed;

public final class EmbeddingUniforms {
	/**
	 * Only used by cull shaders.
	 */
	public static final String USE_MODEL_MATRIX = "_flw_useModelMatrix";
	public static final String MODEL_MATRIX = "_flw_modelMatrix";
	public static final String NORMAL_MATRIX = "_flw_normalMatrix";

	private EmbeddingUniforms() {
	}
}
