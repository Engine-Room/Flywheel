package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.compile.ContextShaders;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

public class GlobalEnvironment implements Environment {
	public static final GlobalEnvironment INSTANCE = new GlobalEnvironment();

	private GlobalEnvironment() {
	}

	@Override
	public ContextShader contextShader() {
		return ContextShaders.DEFAULT;
	}

	@Override
	public void setupDraw(GlProgram drawProgram) {

	}

	@Override
	public void setupCull(GlProgram cullProgram) {
		cullProgram.setBool("_flw_useEmbeddedModel", false);
	}
}
