package dev.engine_room.flywheel.backend.engine.embed;

import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public class GlobalEnvironment implements Environment {
	public static final GlobalEnvironment INSTANCE = new GlobalEnvironment();

	private GlobalEnvironment() {
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.DEFAULT;
	}

	@Override
	public void setupDraw(GlProgram drawProgram) {

	}

	@Override
	public void setupCull(GlProgram cullProgram) {
		cullProgram.setBool(EmbeddingUniforms.USE_MODEL_MATRIX, false);
	}

	@Override
	public void acquire() {

	}

	@Override
	public void release() {

	}
}
