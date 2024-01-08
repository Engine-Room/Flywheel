package com.jozufozu.flywheel.backend.compile;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public class IndirectPrograms {
	private static final ResourceLocation CULL_SHADER_MAIN = Flywheel.rl("internal/indirect/cull.glsl");
	private static final ResourceLocation APPLY_SHADER_MAIN = Flywheel.rl("internal/indirect/apply.glsl");
	private static final ResourceLocation SCATTER_SHADER_MAIN = Flywheel.rl("internal/indirect/scatter.glsl");

	public static IndirectPrograms instance;
	private static final Compile<InstanceType<?>> CULL = new Compile<>();
	private static final Compile<ResourceLocation> UTIL = new Compile<>();
	private final Map<PipelineProgramKey, GlProgram> pipeline;
	private final Map<InstanceType<?>, GlProgram> culling;
	private final GlProgram apply;
	private final GlProgram scatter;

	public IndirectPrograms(Map<PipelineProgramKey, GlProgram> pipeline, Map<InstanceType<?>, GlProgram> culling, GlProgram apply, GlProgram scatter) {
		this.pipeline = pipeline;
		this.culling = culling;
		this.apply = apply;
		this.scatter = scatter;
	}

	static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		_delete();
		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, uniformComponent, vertexComponents, fragmentComponents);
		var cullingCompiler = createCullingCompiler(uniformComponent, sources);
		var applyCompiler = createUtilCompiler(sources);

		try {
			var pipelineResult = pipelineCompiler.compileAndReportErrors(pipelineKeys);
			var cullingResult = cullingCompiler.compileAndReportErrors(createCullingKeys());
			var utils = applyCompiler.compileAndReportErrors(List.of(APPLY_SHADER_MAIN, SCATTER_SHADER_MAIN));

			if (pipelineResult != null && cullingResult != null && utils != null) {
				instance = new IndirectPrograms(pipelineResult, cullingResult, utils.get(APPLY_SHADER_MAIN), utils.get(SCATTER_SHADER_MAIN));
			}
		} catch (Throwable e) {
			Flywheel.LOGGER.error("Failed to compile indirect programs", e);
		}
		pipelineCompiler.delete();
		cullingCompiler.delete();
		applyCompiler.delete();
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
		return CULL.program()
				.link(CULL.shader(GlslVersion.V460, ShaderType.COMPUTE)
						.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
						.withComponent(uniformComponent)
						.withComponent(IndirectComponent::create)
						.withResource(InstanceType::cullShader)
						.withResource(CULL_SHADER_MAIN))
				.then((key, program) -> program.setUniformBlockBinding("FlwUniforms", 0))
				.harness(sources);
	}

	private static CompilationHarness<ResourceLocation> createUtilCompiler(ShaderSources sources) {
		return UTIL.program()
				.link(UTIL.shader(GlslVersion.V460, ShaderType.COMPUTE)
						.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
						.withResource(s -> s))
				.harness(sources);
	}

	public GlProgram getIndirectProgram(InstanceType<?> instanceType, Context contextShader) {
		return pipeline.get(new PipelineProgramKey(instanceType, contextShader));
	}

	public GlProgram getCullingProgram(InstanceType<?> instanceType) {
		return culling.get(instanceType);
	}

	public GlProgram getApplyProgram() {
		return apply;
	}

	public GlProgram getScatterProgram() {
		return scatter;
	}

	public void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
		culling.values()
				.forEach(GlProgram::delete);
		apply.delete();
	}
}
