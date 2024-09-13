package dev.engine_room.flywheel.backend.compile;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.backend.compile.component.InstanceStructComponent;
import dev.engine_room.flywheel.backend.compile.component.SsboInstanceComponent;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness;
import dev.engine_room.flywheel.backend.compile.core.Compile;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.util.AtomicReferenceCounted;
import dev.engine_room.flywheel.lib.util.ResourceUtil;
import net.minecraft.resources.ResourceLocation;

public class IndirectPrograms extends AtomicReferenceCounted {
	private static final ResourceLocation CULL_SHADER_API_IMPL = Flywheel.rl("internal/indirect/cull_api_impl.glsl");
	private static final ResourceLocation CULL_SHADER_MAIN = Flywheel.rl("internal/indirect/early_cull.glsl");
	private static final ResourceLocation PASS2_SHADER_MAIN = Flywheel.rl("internal/indirect/late_cull.glsl");
	private static final ResourceLocation APPLY_SHADER_MAIN = Flywheel.rl("internal/indirect/apply.glsl");
	private static final ResourceLocation SCATTER_SHADER_MAIN = Flywheel.rl("internal/indirect/scatter.glsl");
	private static final ResourceLocation DEPTH_REDUCE_SHADER_MAIN = Flywheel.rl("internal/indirect/depth_reduce.glsl");
	private static final ResourceLocation READ_VISIBILITY_SHADER_MAIN = Flywheel.rl("internal/indirect/read_visibility.glsl");
	private static final ResourceLocation ZERO_MODELS_SHADER_MAIN = Flywheel.rl("internal/indirect/zero_models.glsl");
	private static final ResourceLocation DOWNSAMPLE_FIRST = Flywheel.rl("internal/indirect/downsample_first.glsl");
	private static final ResourceLocation DOWNSAMPLE_SECOND = Flywheel.rl("internal/indirect/downsample_second.glsl");
	public static final List<ResourceLocation> UTIL_SHADERS = List.of(APPLY_SHADER_MAIN, SCATTER_SHADER_MAIN, DEPTH_REDUCE_SHADER_MAIN, READ_VISIBILITY_SHADER_MAIN, ZERO_MODELS_SHADER_MAIN, DOWNSAMPLE_FIRST, DOWNSAMPLE_SECOND);

	private static final Compile<InstanceType<?>> CULL = new Compile<>();
	private static final Compile<ResourceLocation> UTIL = new Compile<>();

	private static final List<String> EXTENSIONS = getExtensions(GlCompat.MAX_GLSL_VERSION);
	private static final List<String> COMPUTE_EXTENSIONS = getComputeExtensions(GlCompat.MAX_GLSL_VERSION);

	@Nullable
	private static IndirectPrograms instance;

	private final Map<PipelineProgramKey, GlProgram> pipeline;
	private final Map<InstanceType<?>, GlProgram> culling;
	private final Map<InstanceType<?>, GlProgram> cullPassTwo;
	private final Map<ResourceLocation, GlProgram> utils;

	private IndirectPrograms(Map<PipelineProgramKey, GlProgram> pipeline, Map<InstanceType<?>, GlProgram> culling, Map<InstanceType<?>, GlProgram> cullPassTwo, Map<ResourceLocation, GlProgram> utils) {
		this.pipeline = pipeline;
		this.culling = culling;
		this.cullPassTwo = cullPassTwo;
		this.utils = utils;
	}

	private static List<String> getExtensions(GlslVersion glslVersion) {
		var extensions = ImmutableList.<String>builder();
		if (glslVersion.compareTo(GlslVersion.V400) < 0) {
			extensions.add("GL_ARB_gpu_shader5");
		}
		if (glslVersion.compareTo(GlslVersion.V420) < 0) {
			extensions.add("GL_ARB_shading_language_420pack");
		}
		if (glslVersion.compareTo(GlslVersion.V430) < 0) {
			extensions.add("GL_ARB_shader_storage_buffer_object");
		}
		if (glslVersion.compareTo(GlslVersion.V460) < 0) {
			extensions.add("GL_ARB_shader_draw_parameters");
		}
		return extensions.build();
	}

	private static List<String> getComputeExtensions(GlslVersion glslVersion) {
		var extensions = ImmutableList.<String>builder();

		extensions.addAll(EXTENSIONS);

		if (glslVersion.compareTo(GlslVersion.V430) < 0) {
			extensions.add("GL_ARB_compute_shader");
		}
		return extensions.build();
	}

	static void reload(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		if (!GlCompat.SUPPORTS_INDIRECT) {
			return;
		}

		IndirectPrograms newInstance = null;

		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INDIRECT, vertexComponents, fragmentComponents, EXTENSIONS);
		var pass1Compiler = createCullingCompiler(sources, CULL_SHADER_MAIN, "early_cull");
		var pass2Compiler = createCullingCompiler(sources, PASS2_SHADER_MAIN, "late_cull");
		var utilCompiler = createUtilCompiler(sources);
		var cullingKeys = createCullingKeys();

		try {
			var pipelineResult = pipelineCompiler.compileAndReportErrors(pipelineKeys);
			var pass1Result = pass1Compiler.compileAndReportErrors(cullingKeys);
			var pass2Result = pass2Compiler.compileAndReportErrors(cullingKeys);
			var utils = utilCompiler.compileAndReportErrors(UTIL_SHADERS);

			if (pipelineResult != null && pass1Result != null && pass2Result != null && utils != null) {
				newInstance = new IndirectPrograms(pipelineResult, pass1Result, pass2Result, utils);
			}
		} catch (Throwable t) {
			FlwPrograms.LOGGER.error("Failed to compile indirect programs", t);
		}

		pipelineCompiler.delete();
		pass1Compiler.delete();
		pass2Compiler.delete();
		utilCompiler.delete();

		setInstance(newInstance);
	}

	/**
	 * A compiler for cull shaders, parameterized by the instance type.
	 */
	private static CompilationHarness<InstanceType<?>> createCullingCompiler(ShaderSources sources, ResourceLocation main, String name) {
		return CULL.program()
				.link(CULL.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.COMPUTE)
						.nameMapper(instanceType -> name + "/" + ResourceUtil.toDebugFileNameNoExtension(instanceType.cullShader()))
						.requireExtensions(COMPUTE_EXTENSIONS)
						.define("_FLW_SUBGROUP_SIZE", GlCompat.SUBGROUP_SIZE)
						.withResource(CULL_SHADER_API_IMPL)
						.withComponent(InstanceStructComponent::new)
						.withResource(InstanceType::cullShader)
						.withComponent(SsboInstanceComponent::new)
						.withResource(main))
				.postLink((key, program) -> Uniforms.setUniformBlockBindings(program))
				.harness(name, sources);
	}

	/**
	 * A compiler for utility shaders, directly compiles the shader at the resource location specified by the parameter.
	 */
	private static CompilationHarness<ResourceLocation> createUtilCompiler(ShaderSources sources) {
		return UTIL.program()
				.link(UTIL.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.COMPUTE)
						.nameMapper(resourceLocation -> "utilities/" + ResourceUtil.toDebugFileNameNoExtension(resourceLocation))
						.requireExtensions(COMPUTE_EXTENSIONS)
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

	public GlProgram getIndirectProgram(InstanceType<?> instanceType, ContextShader contextShader, LightShader light) {
		return pipeline.get(new PipelineProgramKey(instanceType, contextShader, light));
	}

	public GlProgram getCullingProgram(InstanceType<?> instanceType) {
		return culling.get(instanceType);
	}

	public GlProgram getCullPassTwoProgram(InstanceType<?> instanceType) {
		return cullPassTwo.get(instanceType);
	}

	public GlProgram getApplyProgram() {
		return utils.get(APPLY_SHADER_MAIN);
	}

	public GlProgram getZeroModelProgram() {
		return utils.get(ZERO_MODELS_SHADER_MAIN);
	}

	public GlProgram getScatterProgram() {
		return utils.get(SCATTER_SHADER_MAIN);
	}

	public GlProgram getDepthReduceProgram() {
		return utils.get(DEPTH_REDUCE_SHADER_MAIN);
	}

	public GlProgram getDownsampleFirstProgram() {
		return utils.get(DOWNSAMPLE_FIRST);
	}

	public GlProgram getDownsampleSecondProgram() {
		return utils.get(DOWNSAMPLE_SECOND);
	}

	public GlProgram getReadVisibilityProgram() {
		return utils.get(READ_VISIBILITY_SHADER_MAIN);
	}

	@Override
	protected void _delete() {
		pipeline.values()
				.forEach(GlProgram::delete);
		culling.values()
				.forEach(GlProgram::delete);
		utils.values()
				.forEach(GlProgram::delete);
	}
}
