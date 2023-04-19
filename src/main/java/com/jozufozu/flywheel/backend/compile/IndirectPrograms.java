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

public class IndirectPrograms {
	private static IndirectPrograms instance;
	private final Map<PipelineProgramKey, GlProgram> pipeline;
	private final Map<InstanceType<?>, GlProgram> culling;

	public IndirectPrograms(Map<PipelineProgramKey, GlProgram> pipeline, Map<InstanceType<?>, GlProgram> culling) {
		this.pipeline = pipeline;
		this.culling = culling;
	}

	public static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent) {
		if (instance != null) {
			instance.delete();
		}
		var indirectCompiler = new PipelineCompiler(sources, pipelineKeys, Pipelines.INDIRECT, uniformComponent);
		var cullingCompiler = new CullingCompiler(sources, createCullingKeys(), uniformComponent);
		instance = new IndirectPrograms(indirectCompiler.compile(), cullingCompiler.compile());
		indirectCompiler.delete();
		cullingCompiler.delete();
	}

	private static ImmutableList<InstanceType<?>> createCullingKeys() {
		ImmutableList.Builder<InstanceType<?>> builder = ImmutableList.builder();
		for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
			builder.add(instanceType);
		}
		return builder.build();
	}

	@Nullable
	public static IndirectPrograms get() {
		return instance;
	}

	public GlProgram getIndirectProgram(VertexType vertexType, InstanceType<?> instanceType, Context contextShader) {
		return pipeline.get(new PipelineProgramKey(vertexType, instanceType, contextShader));
	}

	public GlProgram getCullingProgram(InstanceType<?> instanceType) {
		return culling.get(instanceType);
	}

	public void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
		culling.values()
				.forEach(GlProgram::delete);
	}
}
