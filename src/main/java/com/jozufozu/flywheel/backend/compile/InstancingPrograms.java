package com.jozufozu.flywheel.backend.compile;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.pipeline.Pipelines;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.glsl.ShaderSources;

public class InstancingPrograms {
	private static InstancingPrograms instance;
	private final Map<PipelineProgramKey, GlProgram> pipeline;

	public InstancingPrograms(Map<PipelineProgramKey, GlProgram> pipeline) {
		this.pipeline = pipeline;
	}

	public static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent) {
		if (instance != null) {
			instance.delete();
			instance = null;
		}
		var instancingCompiler = new PipelineCompiler(sources, pipelineKeys, Pipelines.INSTANCED_ARRAYS, uniformComponent);
		var result = instancingCompiler.compileAndReportErrors();

		if (result != null) {
			instance = new InstancingPrograms(result);
		}

		instancingCompiler.delete();
	}

	@Nullable
	public static InstancingPrograms get() {
		return instance;
	}

	public static boolean allLoaded() {
		return instance != null;
	}

	public GlProgram get(VertexType vertexType, InstanceType<?> instanceType, Context contextShader) {
		return pipeline.get(new PipelineProgramKey(vertexType, instanceType, contextShader));
	}

	public void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
	}
}
