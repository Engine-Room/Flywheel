package com.jozufozu.flywheel.backend.engine.embed;

import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public interface Environment {
	ContextShader contextShader();

	void setupDraw(GlProgram drawProgram);

	void setupCull(GlProgram cullProgram);

	void delete();
}
