package com.jozufozu.flywheel.backend.compile;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.core.CompilationHarness;
import com.jozufozu.flywheel.backend.compile.core.Compile;
import com.jozufozu.flywheel.backend.context.ContextShader;
import com.jozufozu.flywheel.backend.gl.GlCompat;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.lib.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class IndirectPrograms extends AbstractPrograms {
	private static final ResourceLocation CULL_SHADER_HEADER = Flywheel.rl("internal/indirect/cull_header.glsl");
	private static final ResourceLocation CULL_SHADER_MAIN = Flywheel.rl("internal/indirect/cull.glsl");
	private static final ResourceLocation APPLY_SHADER_MAIN = Flywheel.rl("internal/indirect/apply.glsl");
	private static final ResourceLocation SCATTER_SHADER_MAIN = Flywheel.rl("internal/indirect/scatter.glsl");

	private static final Compile<InstanceType<?>> CULL = new Compile<>();
	private static final Compile<ResourceLocation> UTIL = new Compile<>();

	@Nullable
	private static IndirectPrograms instance;

	private final Map<PipelineProgramKey, GlProgram> pipeline;
	private final Map<InstanceType<?>, GlProgram> culling;
	private final GlProgram apply;
	private final GlProgram scatter;

	private IndirectPrograms(Map<PipelineProgramKey, GlProgram> pipeline, Map<InstanceType<?>, GlProgram> culling, GlProgram apply, GlProgram scatter) {
		this.pipeline = pipeline;
		this.culling = culling;
		this.apply = apply;
		this.scatter = scatter;
	}

	static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		IndirectPrograms newInstance = null;

		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, vertexComponents, fragmentComponents);
		var cullingCompiler = createCullingCompiler(sources);
		var applyCompiler = createUtilCompiler(sources);

		try {
			var pipelineResult = pipelineCompiler.compileAndReportErrors(pipelineKeys);
			var cullingResult = cullingCompiler.compileAndReportErrors(createCullingKeys());
			var utils = applyCompiler.compileAndReportErrors(List.of(APPLY_SHADER_MAIN, SCATTER_SHADER_MAIN));

			if (pipelineResult != null && cullingResult != null && utils != null) {
				newInstance = new IndirectPrograms(pipelineResult, cullingResult, utils.get(APPLY_SHADER_MAIN), utils.get(SCATTER_SHADER_MAIN));
			}
		} catch (Throwable t) {
			FlwPrograms.LOGGER.error("Failed to compile indirect programs", t);
		}

		pipelineCompiler.delete();
		cullingCompiler.delete();
		applyCompiler.delete();

		setInstance(newInstance);
	}

	private static CompilationHarness<InstanceType<?>> createCullingCompiler(ShaderSources sources) {
		return CULL.program()
				.link(CULL.shader(GlslVersion.V460, ShaderType.COMPUTE)
						.nameMapper(instanceType -> "culling/" + ResourceUtil.toDebugFileNameNoExtension(instanceType.cullShader()))
						.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
						.withResource(CULL_SHADER_HEADER)
						.withComponent(IndirectComponent::create)
						.withResource(InstanceType::cullShader)
						.withResource(CULL_SHADER_MAIN))
				.postLink((key, program) -> program.setUniformBlockBinding("_FlwFrameUniforms", 0))
				.harness("culling", sources);
	}

	private static CompilationHarness<ResourceLocation> createUtilCompiler(ShaderSources sources) {
		return UTIL.program()
				.link(UTIL.shader(GlslVersion.V460, ShaderType.COMPUTE)
						.nameMapper(resourceLocation -> "utilities/" + ResourceUtil.toDebugFileNameNoExtension(resourceLocation))
						.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
						.withResource(s -> s))
				.harness("utilities", sources);
	}

	private static ImmutableList<InstanceType<?>> createCullingKeys() {
		ImmutableList.Builder<InstanceType<?>> builder = ImmutableList.builder();
		for (InstanceType<?> instanceType : InstanceType.REGISTRY) {
			builder.add(instanceType);
		}
		return builder.build();
	}

	static void setInstance(@Nullable IndirectPrograms newInstance) {
		if (instance != null) {
			instance.release();
		}
		if (newInstance != null) {
			newInstance.acquire();
		}
		instance = newInstance;
	}

	@Nullable
	public static IndirectPrograms get() {
		return instance;
	}

	public static boolean allLoaded() {
		return instance != null;
	}

	public GlProgram getIndirectProgram(InstanceType<?> instanceType, ContextShader contextShader) {
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

	@Override
	protected void delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
		culling.values()
				.forEach(GlProgram::delete);
		apply.delete();
	}
}
