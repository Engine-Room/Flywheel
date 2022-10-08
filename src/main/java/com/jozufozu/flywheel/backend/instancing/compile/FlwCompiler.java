package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.pipeline.Pipeline;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.indirect.IndirectComponent;
import com.jozufozu.flywheel.core.BackendTypes;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.Pipelines;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.ShaderSources;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.resources.ResourceLocation;

public class FlwCompiler {

	public static FlwCompiler INSTANCE;

	public static void onReloadRenderers(ReloadRenderersEvent t) {

	}

	final Map<PipelineContext, GlProgram> pipelinePrograms = new HashMap<>();
	final Map<StructType<?>, GlProgram> cullingPrograms = new HashMap<>();

	boolean needsCrash = false;

	final long compileStart = System.nanoTime();
	final Multimap<Set<UniformProvider>, PipelineContext> uniformProviderGroups = ArrayListMultimap.create();
	final List<PipelineContext> pipelineContexts = new ArrayList<>();

	private final ShaderSources sources;
	private final VertexMaterialComponent vertexMaterialComponent;
	private final FragmentMaterialComponent fragmentMaterialComponent;

	public FlwCompiler(ShaderSources sources) {
		this.sources = sources;
		this.vertexMaterialComponent = new VertexMaterialComponent(sources, ComponentRegistry.materials.vertexSources());
		this.fragmentMaterialComponent = new FragmentMaterialComponent(sources, ComponentRegistry.materials.fragmentSources());

		for (SimplePipeline pipelineShader : BackendTypes.availablePipelineShaders()) {
			for (StructType<?> structType : ComponentRegistry.structTypes) {
				for (VertexType vertexType : ComponentRegistry.vertexTypes) {
					// TODO: context ubershaders, or not?
					pipelineContexts.add(new PipelineContext(vertexType, structType, Components.WORLD, pipelineShader));
				}
			}
		}

		// TODO: analyze uniform providers and group them into sets; break up this ctor

		for (PipelineContext context : pipelineContexts) {
			try {
				var glProgram = compilePipelineContext(context);
				pipelinePrograms.put(context, glProgram);
			} catch (ShaderCompilationException e) {
				needsCrash = true;
				Backend.LOGGER.error(e.errors);
			}
		}

		for (StructType<?> type : ComponentRegistry.structTypes) {
			try {
				var glProgram = compileComputeCuller(type);
				cullingPrograms.put(type, glProgram);
			} catch (ShaderCompilationException e) {
				needsCrash = true;
				Backend.LOGGER.error(e.errors);
			}
		}

		finish();
	}

	public void finish() {
		long compileEnd = System.nanoTime();

		Backend.LOGGER.info("Compiled " + pipelineContexts.size() + " programs in " + StringUtil.formatTime(compileEnd - compileStart));

		if (needsCrash) {
			throw new ShaderLoadingException("Compilation failed");
		}
	}

	public GlProgram getPipelineProgram(VertexType vertexType, StructType<?> structType, ContextShader contextShader, SimplePipeline pipelineShader) {
		return pipelinePrograms.get(new PipelineContext(vertexType, structType, contextShader, pipelineShader));
	}

	public GlProgram getCullingProgram(StructType<?> structType) {
		return cullingPrograms.get(structType);
	}

	protected GlProgram compilePipelineContext(PipelineContext ctx) throws ShaderCompilationException {

		var glslVersion = ctx.pipelineShader()
				.glslVersion();

		var vertex = compileShader(glslVersion, ShaderType.VERTEX, getVertexComponents(ctx));
		var fragment = compileShader(glslVersion, ShaderType.FRAGMENT, getFragmentComponents(ctx));

		return ctx.contextShader()
				.factory()
				.create(new ProgramAssembler().attachShader(vertex)
						.attachShader(fragment)
						.link());
	}

	protected GlProgram compileComputeCuller(StructType<?> structType) {

		return new GlProgram(new ProgramAssembler().attachShader(compileShader(GLSLVersion.V460, ShaderType.COMPUTE, getComputeComponents(structType)))
				.link());
	}

	ImmutableList<SourceComponent> getVertexComponents(PipelineContext ctx) {
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

	ImmutableList<SourceComponent> getFragmentComponents(PipelineContext ctx) {
		var context = sources.find(ctx.contextShader()
				.fragmentShader()
				.resourceLocation());
		var pipeline = sources.find(ctx.pipelineShader()
				.fragment()
				.resourceLocation());
		return ImmutableList.of(fragmentMaterialComponent, context, pipeline);
	}

	@NotNull ImmutableList<SourceComponent> getComputeComponents(StructType<?> structType) {
		var instanceAssembly = new IndirectComponent(sources, structType);
		var instance = sources.find(structType.getInstanceShader()
				.resourceLocation());
		var pipeline = sources.find(Pipelines.Files.INDIRECT_CULL.resourceLocation());

		return ImmutableList.of(instanceAssembly, instance, pipeline);
	}

	protected GlShader compileShader(GLSLVersion glslVersion, ShaderType shaderType, ImmutableList<SourceComponent> sourceComponents) {
		StringBuilder finalSource = new StringBuilder(CompileUtil.generateHeader(glslVersion, shaderType));
		finalSource.append("#extension GL_ARB_explicit_attrib_location : enable\n");
		finalSource.append("#extension GL_ARB_conservative_depth : enable\n");

		var ctx = new CompilationContext();
		var names = ImmutableList.<ResourceLocation>builder();

		for (var include : depthFirstInclude(sourceComponents)) {
			appendFinalSource(finalSource, ctx, include);
		}

		for (var component : sourceComponents) {
			appendFinalSource(finalSource, ctx, component);
			names.add(component.name());
		}

		try {
			return new GlShader(finalSource.toString(), shaderType, names.build());
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(ctx);
		}
	}

	private static void appendFinalSource(StringBuilder finalSource, CompilationContext ctx, SourceComponent component) {
		var source = component.source();

		if (component instanceof SourceFile file) {
			finalSource.append(ctx.sourceHeader(file));
		} else {
			finalSource.append(ctx.generatedHeader(source, component.name()
					.toString()));
		}

		finalSource.append(source);
	}

	protected static Set<SourceComponent> depthFirstInclude(ImmutableList<SourceComponent> root) {
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : root) {
			recursiveDepthFirstInclude(included, component);
		}
		return included;
	}

	protected static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
		for (var include : component.included()) {
			recursiveDepthFirstInclude(included, include);
		}
		included.addAll(component.included());
	}
}
