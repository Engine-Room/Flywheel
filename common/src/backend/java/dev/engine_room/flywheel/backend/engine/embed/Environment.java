package dev.engine_room.flywheel.backend.engine.embed;

import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public interface Environment {
	ContextShader contextShader();

	void setupCull(GlProgram cullProgram);

	void setupDraw(GlProgram drawProgram);
}
