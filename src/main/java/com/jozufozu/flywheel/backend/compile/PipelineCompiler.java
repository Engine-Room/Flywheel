package com.jozufozu.flywheel.backend.compile;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.lib.vertex.BlockVertex;

public class PipelineCompiler {
	private static final Compile<PipelineProgramKey> PIPELINE = new Compile<>();

	static CompilationHarness<PipelineProgramKey> create(ShaderSources sources, Pipeline pipeline, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		return PIPELINE.harness(sources)
				.keys(pipelineKeys)
				.compiler(PIPELINE.program()
						.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.VERTEX)
								.withComponent(uniformComponent)
								.withComponent(key -> pipeline.assembler()
										.assemble(new Pipeline.InstanceAssemblerContext(BlockVertex.FORMAT.getAttributeCount(), key.instanceType())))
								.withResource(pipeline.vertexAPI())
								.withComponents(vertexComponents)
								.withResource(key -> key.instanceType()
										.instanceShader())
								.withResource(key -> key.contextShader()
										.vertexShader())
								.withResource(pipeline.vertexShader()))
						.link(PIPELINE.shader(pipeline.glslVersion(), ShaderType.FRAGMENT)
								.enableExtension("GL_ARB_conservative_depth")
								.withComponent(uniformComponent)
								.withResource(pipeline.fragmentAPI())
								.withComponents(fragmentComponents)
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
