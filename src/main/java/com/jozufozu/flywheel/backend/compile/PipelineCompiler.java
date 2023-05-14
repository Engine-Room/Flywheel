package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

public class PipelineCompiler extends AbstractCompiler<PipelineProgramKey> {
	private final Pipeline pipeline;
	private final List<SourceComponent> vertexPrelude = new ArrayList<>();
	private final List<SourceComponent> vertexPostlude = new ArrayList<>();
	private final List<SourceComponent> fragmentPrelude = new ArrayList<>();
	private final List<SourceComponent> fragmentPostlude = new ArrayList<>();

	public PipelineCompiler(ShaderSources sources, ImmutableList<PipelineProgramKey> keys, Pipeline pipeline) {
		super(sources, keys);
		this.pipeline = pipeline;
	}

	static PipelineCompiler create(SourceLoader sourceLoader, Pipeline pipeline, ImmutableList<PipelineProgramKey> pipelineKeys, UniformComponent uniformComponent, MaterialAdapterComponent vertexMaterialComponent, MaterialAdapterComponent fragmentMaterialComponent) {
		var fragmentPipeline = sourceLoader.find(pipeline.fragmentShader());
		var vertexPipeline = sourceLoader.find(pipeline.vertexShader());

		return new PipelineCompiler(sourceLoader.sources, pipelineKeys, pipeline).addPrelude(uniformComponent)
				.addFragmentPrelude(fragmentMaterialComponent)
				.addVertexPrelude(vertexMaterialComponent)
				.addFragmentPostlude(fragmentPipeline)
				.addVertexPostlude(vertexPipeline);
	}

	public PipelineCompiler addPrelude(SourceComponent component) {
		addVertexPrelude(component);
		addFragmentPrelude(component);
		return this;
	}

	public PipelineCompiler addVertexPrelude(SourceComponent component) {
		vertexPrelude.add(component);
		return this;
	}

	public PipelineCompiler addVertexPostlude(SourceComponent component) {
		vertexPostlude.add(component);
		return this;
	}

	public PipelineCompiler addFragmentPrelude(SourceComponent component) {
		fragmentPrelude.add(component);
		return this;
	}

	public PipelineCompiler addFragmentPostlude(SourceComponent component) {
		fragmentPostlude.add(component);
		return this;
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
				.assemble(new Pipeline.InstanceAssemblerContext(sourceLoader, key.vertexType(), key.instanceType()));

		var layout = sourceLoader.find(key.vertexType()
				.layoutShader());
		var instance = sourceLoader.find(key.instanceType()
				.instanceShader());
		var context = sourceLoader.find(key.contextShader()
				.vertexShader());

		if (instanceAssembly == null || layout == null || instance == null || context == null) {
			return null;
		}

		// Check this here to do a full dry-run in case of a preloading error.
		if (vertexPrelude.stream()
				.anyMatch(Objects::isNull) || vertexPostlude.stream()
				.anyMatch(Objects::isNull)) {
			return null;
		}

		return ImmutableList.<SourceComponent>builder()
				.addAll(vertexPrelude)
				.add(instanceAssembly, layout, instance, context)
				.addAll(vertexPostlude)
				.build();
	}

	@Nullable
	private List<SourceComponent> getFragmentComponents(PipelineProgramKey key) {
		ResourceLocation rl = key.contextShader()
				.fragmentShader();
		var context = sourceLoader.find(rl);

		if (context == null) {
			return null;
		}

		// Check this here to do a full dry-run in case of a preloading error.
		if (fragmentPrelude.stream()
				.anyMatch(Objects::isNull) || fragmentPostlude.stream()
				.anyMatch(Objects::isNull)) {
			return null;
		}

		return ImmutableList.<SourceComponent>builder()
				.addAll(fragmentPrelude)
				.add(context)
				.addAll(fragmentPostlude)
				.build();
	}
}
