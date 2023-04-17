package com.jozufozu.flywheel.backend.compile;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.pipeline.Pipeline;
import com.jozufozu.flywheel.backend.compile.pipeline.Pipelines;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.error.ErrorReporter;
import com.jozufozu.flywheel.lib.context.Contexts;

import net.minecraft.server.packs.resources.ResourceManager;

public class FlwPrograms {
	private static FlwPrograms instance;

	private final Map<PipelineProgramKey, GlProgram> pipelinePrograms;
	private final Map<InstanceType<?>, GlProgram> cullingPrograms;

	public FlwPrograms(Map<PipelineProgramKey, GlProgram> pipelinePrograms, Map<InstanceType<?>, GlProgram> cullingPrograms) {
		this.pipelinePrograms = pipelinePrograms;
		this.cullingPrograms = cullingPrograms;
	}

	public static void reload(ResourceManager resourceManager) {
		if (instance != null) {
			instance.delete();
		}

		ErrorReporter errorReporter = new ErrorReporter();
		ShaderSources sources = new ShaderSources(errorReporter, resourceManager);
		FlwCompiler compiler = new FlwCompiler(sources, createPipelineKeys(), createCullingKeys());
		instance = compiler.compile();
		compiler.delete();
	}

	private static ImmutableList<PipelineProgramKey> createPipelineKeys() {
		ImmutableList.Builder<PipelineProgramKey> builder = ImmutableList.builder();
		for (Pipeline pipelineShader : Pipelines.ALL) {
			for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
				for (VertexType vertexType : VertexType.REGISTRY) {
					builder.add(new PipelineProgramKey(vertexType, instanceType, Contexts.WORLD, pipelineShader));
				}
			}
		}
		return builder.build();
	}

	private static ImmutableList<InstanceType<?>> createCullingKeys() {
		ImmutableList.Builder<InstanceType<?>> builder = ImmutableList.builder();
		for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
			builder.add(instanceType);
		}
		return builder.build();
	}

	@Nullable
	public static FlwPrograms get() {
		return instance;
	}

	public GlProgram getPipelineProgram(VertexType vertexType, InstanceType<?> instanceType, Context contextShader, Pipeline pipelineShader) {
		return pipelinePrograms.get(new PipelineProgramKey(vertexType, instanceType, contextShader, pipelineShader));
	}

	public GlProgram getCullingProgram(InstanceType<?> instanceType) {
		return cullingPrograms.get(instanceType);
	}

	private void delete() {
		pipelinePrograms.values()
				.forEach(GlProgram::delete);
		cullingPrograms.values()
				.forEach(GlProgram::delete);
	}

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param vertexType     The vertex type the program should be adapted for.
	 * @param instanceType   The instance shader to use.
	 * @param contextShader  The context shader to use.
	 * @param pipelineShader The pipeline shader to use.
	 */
	public record PipelineProgramKey(VertexType vertexType, InstanceType<?> instanceType, Context contextShader,
									 Pipeline pipelineShader) {
	}
}
