package com.jozufozu.flywheel.api.context;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.source.FileResolution;

public interface Context {
	void setup(GlProgram program);

	FileResolution vertexShader();

	FileResolution fragmentShader();
}
