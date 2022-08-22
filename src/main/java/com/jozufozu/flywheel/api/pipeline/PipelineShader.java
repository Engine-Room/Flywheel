package com.jozufozu.flywheel.api.pipeline;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.source.FileResolution;

public record PipelineShader(GLSLVersion glslVersion, FileResolution vertex, FileResolution fragment) {

}
