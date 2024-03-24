package com.jozufozu.flywheel.backend.compile;

import java.util.List;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.compile.component.InstanceStructComponent;
import com.jozufozu.flywheel.backend.compile.core.CompilationHarness;
import com.jozufozu.flywheel.backend.compile.core.Compile;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.lib.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class PipelineCompiler {
	private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

	private static final ResourceLocation API_IMPL_VERT = Flywheel.rl("internal/api_impl.vert");
	private static final ResourceLocation API_IMPL_FRAG = Flywheel.rl("internal/api_impl.frag");

	static CompilationHarness<PipelineProgramKey> create(ShaderSources sources, Pipeline pipeline, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		return PIPELINE.program()
				.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.VERTEX)
						.nameMapper(key -> {
							var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType()
									.vertexShader());

							var context = key.contextShader()
									.nameLowerCase();
							return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "_" + context;
						})
						.onCompile((key, comp) -> key.contextShader()
								.onCompile(comp))
						.withResource(API_IMPL_VERT)
						.withComponent(key -> new InstanceStructComponent(key.instanceType()))
						.withResource(key -> key.instanceType()
								.vertexShader())
						.withComponents(vertexComponents)
						.withResource(InternalVertex.LAYOUT_SHADER)
						.withComponent(key -> pipeline.assembler()
								.assemble(key.instanceType()))
						.withResource(pipeline.vertexMain()))
				.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.FRAGMENT)
						.nameMapper(key -> {
							var context = key.contextShader()
									.nameLowerCase();
							return "pipeline/" + pipeline.compilerMarker() + "/" + context;
						})
						.enableExtension("GL_ARB_conservative_depth")
						.onCompile((key, comp) -> key.contextShader()
								.onCompile(comp))
						.withResource(API_IMPL_FRAG)
						.withComponents(fragmentComponents)
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
					program.setUniformBlockBinding("_FlwFrameUniforms", Uniforms.FRAME_INDEX);
					program.setUniformBlockBinding("_FlwFogUniforms", Uniforms.FOG_INDEX);
					program.setUniformBlockBinding("_FlwOptionsUniforms", Uniforms.OPTIONS_INDEX);
					program.setUniformBlockBinding("_FlwPlayerUniforms", Uniforms.PLAYER_INDEX);
					program.setUniformBlockBinding("_FlwLevelUniforms", Uniforms.LEVEL_INDEX);

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
	}
}
