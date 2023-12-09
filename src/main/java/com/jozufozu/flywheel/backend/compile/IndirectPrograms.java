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
import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.GlslVersion;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.lib.util.Unit;

import net.minecraft.resources.ResourceLocation;

public class IndirectPrograms {
	private static final ResourceLocation CULL_SHADER_MAIN = Flywheel.rl("internal/indirect/cull.glsl");
	private static final ResourceLocation APPLY_SHADER_MAIN = Flywheel.rl("internal/indirect/apply.glsl");
	private static final ResourceLocation SCATTER_SHADER_MAIN = Flywheel.rl("internal/indirect/scatter.glsl");

	public static IndirectPrograms instance;
	private static final Compile<InstanceType<?>> CULL = new Compile<>();
	private static final Compile<Unit> UNIT = new Compile<>();
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
		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, pipelineKeys, uniformComponent, vertexComponents, fragmentComponents);
		var cullingCompiler = createCullingCompiler(uniformComponent, sources);
		var applyCompiler = createApplyCompiler(sources);
		var scatterCompiler = createScatterCompiler(sources);

		try {
			var pipelineResult = pipelineCompiler.compileAndReportErrors();
			var cullingResult = cullingCompiler.compileAndReportErrors();
			var applyResult = applyCompiler.compileAndReportErrors();
			var scatterResult = scatterCompiler.compileAndReportErrors();

			if (pipelineResult != null && cullingResult != null && applyResult != null && scatterResult != null) {
				instance = new IndirectPrograms(pipelineResult, cullingResult, applyResult.get(Unit.INSTANCE), scatterResult.get(Unit.INSTANCE));
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
		return CULL.harness(sources)
				.keys(createCullingKeys())
				.compiler(CULL.program()
						.link(CULL.shader(GlslVersion.V460, ShaderType.COMPUTE)
								.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
								.withComponent(uniformComponent)
								.withComponent(IndirectComponent::create)
								.withResource(InstanceType::cullShader)
								.withResource(CULL_SHADER_MAIN))
						.then((key, program) -> program.setUniformBlockBinding("FlwUniforms", 0)))
				.build();
	}

	private static CompilationHarness<Unit> createApplyCompiler(ShaderSources sources) {
		return UNIT.harness(sources)
				.keys(ImmutableList.of(Unit.INSTANCE))
				.compiler(UNIT.program()
						.link(UNIT.shader(GlslVersion.V460, ShaderType.COMPUTE)
								.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
								.withResource(APPLY_SHADER_MAIN)))
				.build();
	}

	private static CompilationHarness<Unit> createScatterCompiler(ShaderSources sources) {
		return UNIT.harness(sources)
				.keys(ImmutableList.of(Unit.INSTANCE))
				.compiler(UNIT.program()
						.link(UNIT.shader(GlslVersion.V460, ShaderType.COMPUTE)
								.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
								.withResource(SCATTER_SHADER_MAIN)))
				.build();
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
