package com.jozufozu.flywheel.backend.compile;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;

public class InstancingPrograms {
	static InstancingPrograms instance;
	private final Map<PipelineProgramKey, GlProgram> pipeline;

	public InstancingPrograms(Map<PipelineProgramKey, GlProgram> pipeline) {
		this.pipeline = pipeline;
	}

	static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		_delete();
		var instancingCompiler = PipelineCompiler.create(sources, Pipelines.INSTANCED_ARRAYS, pipelineKeys, uniformComponent, vertexComponents, fragmentComponents);

		try {
			var result = instancingCompiler.compileAndReportErrors();

			if (result != null) {
				instance = new InstancingPrograms(result);
			}
		} catch (Throwable e) {
			Flywheel.LOGGER.error("Failed to compile instancing programs", e);
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

	static void _delete() {
		if (instance != null) {
			instance.delete();
			instance = null;
		}
	}

	public GlProgram get(InstanceType<?> instanceType, Context contextShader) {
		return pipeline.get(new PipelineProgramKey(instanceType, contextShader));
	}

	public void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
	}
}
