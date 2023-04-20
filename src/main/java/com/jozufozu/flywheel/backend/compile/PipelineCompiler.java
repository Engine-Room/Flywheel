package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.compile.component.MaterialAdapterComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.material.MaterialIndices;

public class PipelineCompiler extends AbstractCompiler<PipelineProgramKey> {
	private final Pipeline pipeline;
	private final MaterialAdapterComponent vertexMaterialComponent;
	private final MaterialAdapterComponent fragmentMaterialComponent;
	private final UniformComponent uniformComponent;
	private final SourceFile pipelineFragment;
	private final SourceFile pipelineVertex;

	public PipelineCompiler(ShaderSources sources, ImmutableList<PipelineProgramKey> keys, Pipeline pipeline, UniformComponent uniformComponent) {
		super(sources, keys);
		this.pipeline = pipeline;
		this.uniformComponent = uniformComponent;

		vertexMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(MaterialIndices.getAllVertexShaders())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_materialVertexID"))
				.build(sources);
		fragmentMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
				.materialSources(MaterialIndices.getAllFragmentShaders())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.literal(false))
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_materialFragmentID"))
				.build(sources);

		pipelineFragment = sources.find(pipeline.fragmentShader());
		pipelineVertex = sources.find(pipeline.vertexShader());
	}

	@Nullable
	@Override
	protected GlProgram compile(PipelineProgramKey key) {
		var glslVersion = pipeline.glslVersion();

		var vertex = shaderCompiler.compile(glslVersion, ShaderType.VERTEX, getVertexComponents(key));
		var fragment = shaderCompiler.compile(glslVersion, ShaderType.FRAGMENT, getFragmentComponents(key));

		if (vertex == null || fragment == null) {
			return null;
		}

		var glProgram = programLinker.link(vertex, fragment);
		key.contextShader()
				.onProgramLink(glProgram);
		return glProgram;
	}

	private ImmutableList<SourceComponent> getVertexComponents(PipelineProgramKey key) {
		var instanceAssembly = pipeline.assembler()
				.assemble(new Pipeline.InstanceAssemblerContext(sources, key.vertexType(), key.instanceType()));

		var layout = sources.find(key.vertexType()
				.layoutShader());
		var instance = sources.find(key.instanceType()
				.instanceShader());
		var context = sources.find(key.contextShader()
				.vertexShader());

		return ImmutableList.of(uniformComponent, vertexMaterialComponent, instanceAssembly, layout, instance, context, pipelineVertex);
	}

	private ImmutableList<SourceComponent> getFragmentComponents(PipelineProgramKey key) {
		var context = sources.find(key.contextShader()
				.fragmentShader());
		return ImmutableList.of(uniformComponent, fragmentMaterialComponent, context, pipelineFragment);
	}
}
