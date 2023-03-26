package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.source.FileResolution;

public record ContextShader(FileResolution vertexShader, FileResolution fragmentShader) implements Context {
	@Override
	public void onProgramLink(GlProgram program) {
		program.bind();
		program.setSamplerBinding("flw_diffuseTex", 0);
		program.setSamplerBinding("flw_overlayTex", 1);
		program.setSamplerBinding("flw_lightTex", 2);
		GlProgram.unbind();
	}

	@Override
	public FileResolution vertexShader() {
		return vertexShader;
	}

	@Override
	public FileResolution fragmentShader() {
		return fragmentShader;
	}
}
