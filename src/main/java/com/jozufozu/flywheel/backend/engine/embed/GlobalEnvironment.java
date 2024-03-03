package com.jozufozu.flywheel.backend.engine.embed;

import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

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
		cullProgram.setBool("_flw_useEmbeddedModel", false);
	}

	@Override
	public void acquire() {

	}

	@Override
	public void release() {

	}
}
