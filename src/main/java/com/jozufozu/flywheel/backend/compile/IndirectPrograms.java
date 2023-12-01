package com.jozufozu.flywheel.backend.compile;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.ShaderSources;

import net.minecraft.resources.ResourceLocation;

public class IndirectPrograms {
	public static IndirectPrograms instance;
	private static final Compile<InstanceType<?>> CULL = new Compile<>();
	private final Map<PipelineProgramKey, GlProgram> pipeline;
	private final Map<InstanceType<?>, GlProgram> culling;

	public IndirectPrograms(Map<PipelineProgramKey, GlProgram> pipeline, Map<InstanceType<?>, GlProgram> culling) {
		this.pipeline = pipeline;
		this.culling = culling;
	}

	static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent) {
		_delete();
		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, pipelineKeys, uniformComponent, vertexMaterialComponent, fragmentMaterialComponent);
		var cullingCompiler = createCullingCompiler(uniformComponent, sources);

		try {
			var pipelineResult = pipelineCompiler.compileAndReportErrors();
			var cullingResult = cullingCompiler.compileAndReportErrors();

			if (pipelineResult != null && cullingResult != null) {
				instance = new IndirectPrograms(pipelineResult, cullingResult);
			}
		} catch (Throwable e) {
			Flywheel.LOGGER.error("Failed to compile indirect programs", e);
		}
		pipelineCompiler.delete();
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

	public static boolean allLoaded() {
		return instance != null;
	}

	private static void _delete() {
		if (instance != null) {
			instance.delete();
			instance = null;
		}
	}

	private static CompilationHarness<InstanceType<?>> createCullingCompiler(UniformComponent uniformComponent, ShaderSources sources) {
		return CULL.harness(sources)
				.keys(createCullingKeys())
				.compiler(CULL.program()
						.link(CULL.shader(GLSLVersion.V460, ShaderType.COMPUTE)
								.define("FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
								.withComponent(uniformComponent)
								.withComponent(IndirectComponent::create)
								.withResource(Files.INDIRECT_CULL)
								.withResource(InstanceType::instanceShader))
						.then((key, program) -> program.setUniformBlockBinding("FLWUniforms", 0)))
				.build();
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

	private static final class Files {
		public static final ResourceLocation INDIRECT_CULL = Flywheel.rl("internal/indirect_cull.glsl");
	}
}
