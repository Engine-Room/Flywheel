package com.jozufozu.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;

public class PipelineCompiler {
	static CompilationHarness<PipelineProgramKey> create(ShaderSources sources, Pipeline pipeline, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent) {
		return new CompilationHarness<>(sources, pipelineKeys, Compile.<PipelineProgramKey>program()
				.link(Compile.<PipelineProgramKey>shader(pipeline.glslVersion(), ShaderType.VERTEX)
						.withComponent(uniformComponent)
						.withComponent(vertexMaterialComponent)
						.withComponent(key -> pipeline.assembler()
								.assemble(new Pipeline.InstanceAssemblerContext(key.vertexType(), key.instanceType())))
						.withResource(key -> key.vertexType()
								.layoutShader())
						.withResource(key -> key.instanceType()
								.instanceShader())
						.withResource(key -> key.contextShader()
								.vertexShader())
						.withResource(pipeline.vertexShader()))
				.link(Compile.<PipelineProgramKey>shader(pipeline.glslVersion(), ShaderType.FRAGMENT)
						.enableExtension("GL_ARB_conservative_depth")
						.withComponent(uniformComponent)
						.withComponent(fragmentMaterialComponent)
						.withResource(key -> key.contextShader()
								.fragmentShader())
						.withResource(pipeline.fragmentShader()))
				.then((PipelineProgramKey key, GlProgram program) -> {
					key.contextShader()
							.onProgramLink(program);
					program.setUniformBlockBinding("FLWUniforms", 0);
				}));
	}
}
