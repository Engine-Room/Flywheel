package com.jozufozu.flywheel.backend.compile;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;

public class PipelineCompiler extends AbstractCompiler<PipelineProgramKey> {
	private final Pipeline pipeline;
	private final MaterialAdapterComponent vertexMaterialComponent;
	private final MaterialAdapterComponent fragmentMaterialComponent;
	private final UniformComponent uniformComponent;
	private final SourceFile pipelineFragment;
	private final SourceFile pipelineVertex;

	public PipelineCompiler(ShaderSources sources, ImmutableList<PipelineProgramKey> keys, Pipeline pipeline, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent, UniformComponent uniformComponent) {
		super(sources, keys);
		this.pipeline = pipeline;
		this.vertexMaterialComponent = vertexMaterialComponent;
		this.fragmentMaterialComponent = fragmentMaterialComponent;
		this.uniformComponent = uniformComponent;

		pipelineFragment = this.sources.find(pipeline.fragmentShader())
				.unwrap();
		pipelineVertex = this.sources.find(pipeline.vertexShader())
				.unwrap();
	}

	@Nullable
	@Override
	protected GlProgram compile(PipelineProgramKey key) {
		GlShader vertex = compileVertex(key);
		GlShader fragment = compileFragment(key);

		if (vertex == null || fragment == null) {
			return null;
		}

		var glProgram = programLinker.link(vertex, fragment);
		key.contextShader()
				.onProgramLink(glProgram);
		return glProgram;
	}

	@Nullable
	private GlShader compileVertex(PipelineProgramKey key) {
		var vertexComponents = getVertexComponents(key);
		if (vertexComponents == null) {
			return null;
		}

		return shaderCompiler.compile(pipeline.glslVersion(), ShaderType.VERTEX, vertexComponents);
	}

	@Nullable
	private GlShader compileFragment(PipelineProgramKey key) {
		var fragmentComponents = getFragmentComponents(key);
		if (fragmentComponents == null) {
			return null;
		}

		return shaderCompiler.compile(pipeline.glslVersion(), ShaderType.FRAGMENT, fragmentComponents);
	}

	@Nullable
	private List<SourceComponent> getVertexComponents(PipelineProgramKey key) {
		var instanceAssembly = pipeline.assembler()
				.assemble(new Pipeline.InstanceAssemblerContext(sources, key.vertexType(), key.instanceType()));

		var layout = findOrReport(key.vertexType()
				.layoutShader());
		var instance = findOrReport(key.instanceType()
				.instanceShader());
		var context = findOrReport(key.contextShader()
				.vertexShader());

		if (instanceAssembly == null || layout == null || instance == null || context == null) {
			return null;
		}

		return ImmutableList.of(uniformComponent, vertexMaterialComponent, instanceAssembly, layout, instance, context, pipelineVertex);
	}

	@Nullable
	private List<SourceComponent> getFragmentComponents(PipelineProgramKey key) {
		var context = findOrReport(key.contextShader()
				.fragmentShader());

		if (context == null) {
			return null;
		}

		return ImmutableList.of(uniformComponent, fragmentMaterialComponent, context, pipelineFragment);
	}
}
