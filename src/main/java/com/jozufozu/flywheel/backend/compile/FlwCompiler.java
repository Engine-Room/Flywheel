package com.jozufozu.flywheel.backend.compile;

import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Pipelines;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectComponent;
import com.jozufozu.flywheel.gl.GLSLVersion;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderLoadingException;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.util.StringUtil;

public class FlwCompiler {

	public static FlwCompiler INSTANCE;

	final long compileStart = System.nanoTime();
	private final ShaderSources sources;
	private final UniformComponent uniformComponent;
	private final MaterialAdapterComponent vertexMaterialComponent;
	private final MaterialAdapterComponent fragmentMaterialComponent;

	private final PipelineContextSet pipelineContexts;
	private final CullingContextSet cullingContexts;

	final ShaderCompiler shaderCompiler;
	final Map<PipelineContext, GlProgram> pipelinePrograms = new HashMap<>();
	final Map<InstanceType<?>, GlProgram> cullingPrograms = new HashMap<>();
	final List<FailedCompilation> errors = new ArrayList<>();

	public FlwCompiler(ShaderSources sources) {
		this.shaderCompiler = ShaderCompiler.builder()
				.errorConsumer(errors::add)
				.build();

		this.sources = sources;
		this.vertexMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(MaterialIndices.getAllVertexShaders())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("flw_materialVertexID"))
				.build(sources);
		this.fragmentMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
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
			.switchOn(GlslExpr.variable("flw_materialFragmentID"))
			.build(sources);
		this.uniformComponent = UniformComponent.builder(Flywheel.rl("uniforms"))
			.sources(ShaderUniforms.REGISTRY.getAll()
					.stream()
					.map(ShaderUniforms::uniformShader)
				.toList())
			.build(sources);

		this.pipelineContexts = PipelineContextSet.create();
		this.cullingContexts = CullingContextSet.create();

		doCompilation();

		finish();
	}

	private void doCompilation() {
		for (var ctx : pipelineContexts.all()) {
			compilePipelineContext(ctx);
		}

		for (var ctx : cullingContexts.all()) {
			compileComputeCuller(ctx);
		}
	}

	private void finish() {
		long compileEnd = System.nanoTime();
		int programCount = pipelineContexts.size() + InstanceType.REGISTRY.getAll().size();
		int shaderCount = shaderCompiler.shaderCount();
		int errorCount = errors.size();
		var elapsed = StringUtil.formatTime(compileEnd - compileStart);

		Flywheel.LOGGER.info("Compiled " + programCount + " programs and " + shaderCount + " shaders in " + elapsed + " with " + errorCount + " errors.");

		if (errorCount > 0) {
			var details = errors.stream()
					.map(FailedCompilation::getMessage)
					.collect(Collectors.joining("\n"));
			throw new ShaderLoadingException("Compilation failed.\n" + details);
		}
	}

	public void delete() {
		pipelinePrograms.values()
				.forEach(GlProgram::delete);
		cullingPrograms.values()
				.forEach(GlProgram::delete);
		shaderCompiler.delete();
	}

	public GlProgram getPipelineProgram(VertexType vertexType, InstanceType<?> instanceType, Context contextShader, Pipeline pipelineShader) {
		return pipelinePrograms.get(new PipelineContext(vertexType, instanceType, contextShader, pipelineShader));
	}

	public GlProgram getCullingProgram(InstanceType<?> instanceType) {
		return cullingPrograms.get(instanceType);
	}

	private void compilePipelineContext(PipelineContext ctx) {
		var glslVersion = ctx.pipelineShader()
				.glslVersion();

		var vertex = shaderCompiler.compile(glslVersion, ShaderType.VERTEX, getVertexComponents(ctx));
		var fragment = shaderCompiler.compile(glslVersion, ShaderType.FRAGMENT, getFragmentComponents(ctx));

		if (vertex == null || fragment == null) {
			return;
		}

		var glProgram = link(vertex.handle(), fragment.handle());
		ctx.contextShader()
			.onProgramLink(glProgram);
		pipelinePrograms.put(ctx, glProgram);
	}

	private void compileComputeCuller(CullingContext ctx) {
		var computeComponents = getComputeComponents(ctx.instanceType());
		var result = shaderCompiler.compile(GLSLVersion.V460, ShaderType.COMPUTE, computeComponents);

		if (result == null) {
			return;
		}

		cullingPrograms.put(ctx.instanceType(), link(result.handle()));
	}

	private GlProgram link(int... shaders) {
		var handle = glCreateProgram();
		for (var shader : shaders) {
			glAttachShader(handle, shader);
		}
		glLinkProgram(handle);
		CompileUtil.checkLinkLog(handle);
		return new GlProgram(handle);
	}

	private ImmutableList<SourceComponent> getVertexComponents(PipelineContext ctx) {
		var instanceAssembly = ctx.pipelineShader()
				.assemble(new Pipeline.InstanceAssemblerContext(sources, ctx.vertexType(), ctx.instanceType()));

		var layout = sources.find(ctx.vertexType()
				.layoutShader());
		var instance = sources.find(ctx.instanceType()
				.instanceShader());
		var context = sources.find(ctx.contextShader()
				.vertexShader());
		var pipeline = sources.find(ctx.pipelineShader()
				.vertexShader());

		return ImmutableList.of(uniformComponent, vertexMaterialComponent, instanceAssembly, layout, instance, context, pipeline);
	}

	private ImmutableList<SourceComponent> getFragmentComponents(PipelineContext ctx) {
		var context = sources.find(ctx.contextShader()
				.fragmentShader());
		var pipeline = sources.find(ctx.pipelineShader()
				.fragmentShader());
		return ImmutableList.of(uniformComponent, fragmentMaterialComponent, context, pipeline);
	}

	private ImmutableList<SourceComponent> getComputeComponents(InstanceType<?> instanceType) {
		var instanceAssembly = new IndirectComponent(sources, instanceType);
		var instance = sources.find(instanceType.instanceShader());
		var pipeline = sources.find(Pipelines.Files.INDIRECT_CULL);

		return ImmutableList.of(uniformComponent, instanceAssembly, instance, pipeline);
	}

}
