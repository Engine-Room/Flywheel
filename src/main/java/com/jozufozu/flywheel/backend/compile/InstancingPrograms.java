package com.jozufozu.flywheel.backend.compile;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;

public class InstancingPrograms {
	static InstancingPrograms instance;
	private final Map<PipelineProgramKey, GlProgram> pipeline;

	public InstancingPrograms(Map<PipelineProgramKey, GlProgram> pipeline) {
		this.pipeline = pipeline;
	}

	static void reload(SourceLoader loadChecker, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent) {
		_delete();
		var instancingCompiler = PipelineCompiler.create(loadChecker, Pipelines.INSTANCED_ARRAYS, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);

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

	public GlProgram get(VertexType vertexType, InstanceType<?> instanceType, Context contextShader) {
		return pipeline.get(new PipelineProgramKey(vertexType, instanceType, contextShader));
	}

	public void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
	}
}
