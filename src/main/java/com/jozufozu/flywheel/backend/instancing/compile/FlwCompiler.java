package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.indirect.IndirectComponent;
import com.jozufozu.flywheel.core.BackendTypes;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.Pipelines;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.generate.FnSignature;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;
import com.jozufozu.flywheel.util.StringUtil;

public class FlwCompiler {

	public static FlwCompiler INSTANCE;

	final long compileStart = System.nanoTime();
	private final ShaderSources sources;
	private final MaterialAdapterComponent vertexMaterialComponent;
	private final MaterialAdapterComponent fragmentMaterialComponent;
	private final List<PipelineContext> pipelineContexts;

	final ShaderCompiler shaderCompiler;
	final Multimap<Set<UniformProvider>, PipelineContext> uniformProviderGroups = ArrayListMultimap.create();
	final Map<PipelineContext, GlProgram> pipelinePrograms = new HashMap<>();
	final Map<StructType<?>, GlProgram> cullingPrograms = new HashMap<>();
	final List<FailedCompilation> errors = new ArrayList<>();

	public FlwCompiler(ShaderSources sources) {
		this.shaderCompiler = new ShaderCompiler(errors::add);
		this.sources = sources;
		this.vertexMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(ComponentRegistry.materials.vertexSources())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("flw_materialVertexID"))
				.build(sources);
		this.fragmentMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
				.materialSources(ComponentRegistry.materials.fragmentSources())
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

		this.pipelineContexts = buildPipelineSet();

		// TODO: analyze uniform providers and group them into sets; break up this ctor

		for (PipelineContext context : pipelineContexts) {
			compilePipelineContext(context);
		}

		for (StructType<?> type : ComponentRegistry.structTypes) {
			compileComputeCuller(type);
		}

		finish();
	}

	private void finish() {
		long compileEnd = System.nanoTime();
		int programCount = pipelineContexts.size() + ComponentRegistry.structTypes.size();
		int shaderCount = shaderCompiler.shaderCount();
		int errorCount = errors.size();
		var elapsed = StringUtil.formatTime(compileEnd - compileStart);

		Backend.LOGGER.info("Compiled " + programCount + " programs and " + shaderCount + " shaders in " + elapsed + " with " + errorCount + " errors.");

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

	public GlProgram getPipelineProgram(VertexType vertexType, StructType<?> structType, ContextShader contextShader, SimplePipeline pipelineShader) {
		return pipelinePrograms.get(new PipelineContext(vertexType, structType, contextShader, pipelineShader));
	}

	public GlProgram getCullingProgram(StructType<?> structType) {
		return cullingPrograms.get(structType);
	}

	private void compilePipelineContext(PipelineContext ctx) {
		var glslVersion = ctx.pipelineShader()
				.glslVersion();

		var vertex = shaderCompiler.compile(glslVersion, ShaderType.VERTEX, getVertexComponents(ctx));
		var fragment = shaderCompiler.compile(glslVersion, ShaderType.FRAGMENT, getFragmentComponents(ctx));

		if (vertex == null || fragment == null) {
			return;
		}

		pipelinePrograms.put(ctx, ctx.contextShader()
				.factory()
				.create(new ProgramAssembler().attachShader(vertex)
						.attachShader(fragment)
						.link()));
	}

	private void compileComputeCuller(StructType<?> structType) {
		var result = shaderCompiler.compile(GLSLVersion.V460, ShaderType.COMPUTE, getComputeComponents(structType));

		if (result == null) {
			return;
		}

		cullingPrograms.put(structType, new GlProgram(new ProgramAssembler().attachShader(result)
				.link()));
	}

	private ImmutableList<SourceComponent> getVertexComponents(PipelineContext ctx) {
		var instanceAssembly = ctx.pipelineShader()
				.assemble(new Pipeline.InstanceAssemblerContext(sources, ctx.vertexType(), ctx.structType()));

		var layout = sources.find(ctx.vertexType()
				.getLayoutShader()
				.resourceLocation());
		var instance = sources.find(ctx.structType()
				.getInstanceShader()
				.resourceLocation());
		var context = sources.find(ctx.contextShader()
				.vertexShader()
				.resourceLocation());
		var pipeline = sources.find(ctx.pipelineShader()
				.vertex()
				.resourceLocation());

		return ImmutableList.of(vertexMaterialComponent, instanceAssembly, layout, instance, context, pipeline);
	}

	private ImmutableList<SourceComponent> getFragmentComponents(PipelineContext ctx) {
		var context = sources.find(ctx.contextShader()
				.fragmentShader()
				.resourceLocation());
		var pipeline = sources.find(ctx.pipelineShader()
				.fragment()
				.resourceLocation());
		return ImmutableList.of(fragmentMaterialComponent, context, pipeline);
	}

	private ImmutableList<SourceComponent> getComputeComponents(StructType<?> structType) {
		var instanceAssembly = new IndirectComponent(sources, structType);
		var instance = sources.find(structType.getInstanceShader()
				.resourceLocation());
		var pipeline = sources.find(Pipelines.Files.INDIRECT_CULL.resourceLocation());

		return ImmutableList.of(instanceAssembly, instance, pipeline);
	}

	private static List<PipelineContext> buildPipelineSet() {
		ImmutableList.Builder<PipelineContext> builder = ImmutableList.builder();
		for (SimplePipeline pipelineShader : BackendTypes.availablePipelineShaders()) {
			for (StructType<?> structType : ComponentRegistry.structTypes) {
				for (VertexType vertexType : ComponentRegistry.vertexTypes) {
					// TODO: context ubershaders, or not?
					builder.add(new PipelineContext(vertexType, structType, Components.WORLD, pipelineShader));
				}
			}
		}
		return builder.build();
	}
}
