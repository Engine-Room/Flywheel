package dev.engine_room.flywheel.backend.compile;

import java.util.Collection;
import java.util.List;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.MaterialShaders;
import dev.engine_room.flywheel.backend.BackendConfig;
import dev.engine_room.flywheel.backend.InternalVertex;
import dev.engine_room.flywheel.backend.MaterialShaderIndices;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.component.InstanceStructComponent;
import dev.engine_room.flywheel.backend.compile.component.UberShaderComponent;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness;
import dev.engine_room.flywheel.backend.compile.core.Compile;
import dev.engine_room.flywheel.backend.engine.uniform.FrameUniforms;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.FnSignature;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.util.ResourceUtil;
import net.minecraft.resources.ResourceLocation;

public final class PipelineCompiler {
	private static final List<PipelineCompiler> ALL = List.of();

	private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

	private static UberShaderComponent FOG;
	private static UberShaderComponent CUTOUT;

	private static final ResourceLocation API_IMPL_VERT = Flywheel.rl("internal/api_impl.vert");
	private static final ResourceLocation API_IMPL_FRAG = Flywheel.rl("internal/api_impl.frag");

	private final CompilationHarness<PipelineProgramKey> harness;

	public PipelineCompiler(CompilationHarness<PipelineProgramKey> harness) {
		this.harness = harness;
	}

	public GlProgram get(InstanceType<?> instanceType, ContextShader contextShader, Material material) {
		var light = material.light();
		var cutout = material.cutout();
		var shaders = material.shaders();
		var fog = material.fog();

		// Tell fogSources to index the fog shader if we haven't seen it before.
		// If it is new, this will trigger a deletion of all programs.
		MaterialShaderIndices.fogSources()
				.index(fog.source());

		boolean useCutout = cutout != CutoutShaders.OFF;

		if (useCutout) {
			// Same thing for cutout.
			MaterialShaderIndices.cutoutSources()
					.index(cutout.source());
		}

		return harness.get(new PipelineProgramKey(instanceType, contextShader, light, shaders, useCutout, FrameUniforms.debugOn()));
	}

	public void delete() {
		harness.delete();
	}

	public static void deleteAll() {
		createFogComponent();
		createCutoutComponent();
		ALL.forEach(PipelineCompiler::delete);
	}

	static PipelineCompiler create(ShaderSources sources, Pipeline pipeline, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents, Collection<String> extensions) {
		// We could technically compile every version of light smoothness ahead of time,
		// but that seems unnecessary as I doubt most folks will be changing this option often.
		var harness = PIPELINE.program()
				.link(PIPELINE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.VERTEX)
						.nameMapper(key -> {
							var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType()
									.vertexShader());

							var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders()
									.vertexSource());
							var context = key.contextShader()
									.nameLowerCase();
							var debug = key.debugEnabled() ? "_debug" : "";
							return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "/" + material + "_" + context + debug;
						})
						.requireExtensions(extensions)
						.onCompile((key, comp) -> key.contextShader()
								.onCompile(comp))
						.onCompile((key, comp) -> BackendConfig.INSTANCE.lightSmoothness()
								.onCompile(comp))
						.onCompile((key, comp) -> {
							if (key.debugEnabled()) {
								comp.define("_FLW_DEBUG");
							}
						})
						.withResource(API_IMPL_VERT)
						.withComponent(key -> new InstanceStructComponent(key.instanceType()))
						.withResource(key -> key.instanceType()
								.vertexShader())
						.withResource(key -> key.materialShaders()
								.vertexSource())
						.withComponents(vertexComponents)
						.withResource(InternalVertex.LAYOUT_SHADER)
						.withComponent(key -> pipeline.assembler()
								.assemble(key.instanceType()))
						.withResource(pipeline.vertexMain()))
				.link(PIPELINE.shader(GlCompat.MAX_GLSL_VERSION, ShaderType.FRAGMENT)
						.nameMapper(key -> {
							var context = key.contextShader()
									.nameLowerCase();

							var material = ResourceUtil.toDebugFileNameNoExtension(key.materialShaders()
									.fragmentSource());

							var light = ResourceUtil.toDebugFileNameNoExtension(key.light()
									.source());
							var debug = key.debugEnabled() ? "_debug" : "";
							var cutout = key.useCutout() ? "_cutout" : "";
							return "pipeline/" + pipeline.compilerMarker() + "/frag/" + material + "/" + light + "_" + context + cutout + debug;
						})
						.requireExtensions(extensions)
						.enableExtension("GL_ARB_conservative_depth")
						.onCompile((key, comp) -> key.contextShader()
								.onCompile(comp))
						.onCompile((key, comp) -> BackendConfig.INSTANCE.lightSmoothness()
								.onCompile(comp))
						.onCompile((key, comp) -> {
							if (key.debugEnabled()) {
								comp.define("_FLW_DEBUG");
							}
						})
						.onCompile((key, comp) -> {
							if (key.useCutout()) {
								comp.define("_FLW_USE_DISCARD");
							}
						})
						.withResource(API_IMPL_FRAG)
						.withResource(key -> key.materialShaders()
								.fragmentSource())
						.withComponents(fragmentComponents)
						.withComponent(key -> FOG)
						.withResource(key -> key.light()
								.source())
						.with((key, fetcher) -> (key.useCutout() ? CUTOUT : fetcher.get(CutoutShaders.OFF.source())))
						.withResource(pipeline.fragmentMain()))
				.preLink((key, program) -> {
					program.bindAttribLocation("_flw_aPos", 0);
					program.bindAttribLocation("_flw_aColor", 1);
					program.bindAttribLocation("_flw_aTexCoord", 2);
					program.bindAttribLocation("_flw_aOverlay", 3);
					program.bindAttribLocation("_flw_aLight", 4);
					program.bindAttribLocation("_flw_aNormal", 5);
				})
				.postLink((key, program) -> {
					Uniforms.setUniformBlockBindings(program);

					program.bind();

					program.setSamplerBinding("flw_diffuseTex", Samplers.DIFFUSE);
					program.setSamplerBinding("flw_overlayTex", Samplers.OVERLAY);
					program.setSamplerBinding("flw_lightTex", Samplers.LIGHT);
					pipeline.onLink()
							.accept(program);
					key.contextShader()
							.onLink(program);

					GlProgram.unbind();
				})
				.harness(pipeline.compilerMarker(), sources);

		return new PipelineCompiler(harness);
	}

	public static void createFogComponent() {
		FOG = UberShaderComponent.builder(Flywheel.rl("fog"))
				.materialSources(MaterialShaderIndices.fogSources()
						.all())
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_uberFogIndex"))
				.build(FlwPrograms.SOURCES);
	}

	private static void createCutoutComponent() {
		CUTOUT = UberShaderComponent.builder(Flywheel.rl("cutout"))
				.materialSources(MaterialShaderIndices.cutoutSources()
						.all())
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.boolLiteral(false))
				.switchOn(GlslExpr.variable("_flw_uberCutoutIndex"))
				.build(FlwPrograms.SOURCES);
	}

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param instanceType  The instance shader to use.
	 * @param contextShader The context shader to use.
	 * @param light         The light shader to use.
	 */
	public record PipelineProgramKey(InstanceType<?> instanceType, ContextShader contextShader, LightShader light,
									 MaterialShaders materialShaders, boolean useCutout, boolean debugEnabled) {
	}
}
