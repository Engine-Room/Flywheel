package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;

public record ContextShader(GlProgram.Factory factory, FileResolution vertexShader, FileResolution fragmentShader) {

	public SourceFile getVertexShader() {
		return vertexShader.getFile();
	}

	public SourceFile getFragmentShader() {
		return fragmentShader.getFile();
	}
}
