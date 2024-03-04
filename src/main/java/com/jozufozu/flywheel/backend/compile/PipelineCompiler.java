package com.jozufozu.flywheel.backend.compile;

import java.util.List;

import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.compile.core.CompilationHarness;
import com.jozufozu.flywheel.backend.compile.core.Compile;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.lib.util.ResourceUtil;

public class PipelineCompiler {
	private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

	static CompilationHarness<PipelineProgramKey> create(ShaderSources sources, Pipeline pipeline, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		return PIPELINE.program()
				.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.VERTEX)
						.nameMapper(key -> {
							var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType()
									.vertexShader());

							var context = ResourceUtil.toDebugFileNameNoExtension(key.contextShader()
									.vertexShader());
							return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "_" + context;
						})
						.withResource(pipeline.vertexApiImpl())
						.withResource(InternalVertex.LAYOUT_SHADER)
						.withComponent(key -> pipeline.assembler()
								.assemble(new Pipeline.InstanceAssemblerContext(InternalVertex.ATTRIBUTE_COUNT, key.instanceType())))
						.withComponents(vertexComponents)
						.withResource(key -> key.instanceType()
								.vertexShader())
						.withResource(key -> key.contextShader()
								.vertexShader())
						.withResource(pipeline.vertexMain()))
				.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.FRAGMENT)
						.nameMapper(key -> {
							var instance = ResourceUtil.toDebugFileNameNoExtension(key.instanceType()
									.vertexShader());

							var context = ResourceUtil.toDebugFileNameNoExtension(key.contextShader()
									.fragmentShader());
							return "pipeline/" + pipeline.compilerMarker() + "/" + instance + "_" + context;
						})
						.enableExtension("GL_ARB_conservative_depth")
						.withResource(pipeline.fragmentApiImpl())
						.withComponents(fragmentComponents)
						.withResource(key -> key.contextShader()
								.fragmentShader())
						.withResource(pipeline.fragmentMain()))
				.preLink((key, program) -> {
					program.bindAttribLocation("_flw_a_pos", 0);
					program.bindAttribLocation("_flw_a_color", 1);
					program.bindAttribLocation("_flw_a_texCoord", 2);
					program.bindAttribLocation("_flw_a_overlay", 3);
					program.bindAttribLocation("_flw_a_light", 4);
					program.bindAttribLocation("_flw_a_normal", 5);
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
							.onLink()
							.accept(program);

					GlProgram.unbind();
				})
				.harness(pipeline.compilerMarker(), sources);
	}
}
