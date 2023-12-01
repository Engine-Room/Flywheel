package com.jozufozu.flywheel.backend.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;

public class PipelineCompiler {
	private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

	static CompilationHarness<PipelineProgramKey> create(ShaderSources sources, Pipeline pipeline, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent) {
		return PIPELINE.harness(sources)
				.keys(pipelineKeys)
				.compiler(PIPELINE.program()
						.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.VERTEX)
								.withComponent(uniformComponent)
								.withComponent(key -> pipeline.assembler()
										.assemble(new Pipeline.InstanceAssemblerContext(key.vertexType(), key.instanceType())))
								.withResource(pipeline.vertexAPI())
								.withComponent(vertexMaterialComponent)
								.withResource(key -> key.vertexType()
										.layoutShader())
								.withResource(key -> key.instanceType()
										.instanceShader())
								.withResource(key -> key.contextShader()
										.vertexShader())
								.withResource(pipeline.vertexShader()))
						.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.FRAGMENT)
								.enableExtension("GL_ARB_conservative_depth")
								.withComponent(uniformComponent)
								.withResource(pipeline.fragmentAPI())
								.withComponent(fragmentMaterialComponent)
								.withResource(key -> key.contextShader()
										.fragmentShader())
								.withResource(pipeline.fragmentShader()))
						.then((key, program) -> {
							key.contextShader()
									.onProgramLink(program);
							program.setUniformBlockBinding("FLWUniforms", 0);
						}))
				.build();
	}
}
