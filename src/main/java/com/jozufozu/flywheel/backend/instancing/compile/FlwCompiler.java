package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.indirect.IndirectComponent;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

import net.minecraft.resources.ResourceLocation;

public class FlwCompiler {

	public static final FlwCompiler INSTANCE = new FlwCompiler();

	public static void onReloadRenderers(ReloadRenderersEvent t) {

	}

	private final ShaderCompiler shaderCompiler = new ShaderCompiler();
	public final Map<PipelineContext, GlProgram> pipelinePrograms = new HashMap<>();

	public final Map<StructType<?>, GlProgram> cullingPrograms = new HashMap<>();

	private CompilationEnvironment environment;

	FlwCompiler() {

	}

	public void run() {
		environment = new CompilationEnvironment();

		for (PipelineContext context : environment.pipelineContexts) {
			try {
				var glProgram = compilePipelineContext(context);
				pipelinePrograms.put(context, glProgram);
			} catch (ShaderCompilationException e) {
				environment.needsCrash = true;
				Backend.LOGGER.error(e.errors);
			}
		}

		for (StructType<?> type : ComponentRegistry.structTypes) {
			try {
				var glProgram = compileComputeCuller(type);
				cullingPrograms.put(type, glProgram);
			} catch (ShaderCompilationException e) {
				environment.needsCrash = true;
				Backend.LOGGER.error(e.errors);
			}
		}

		environment.finish();
	}

	public GlProgram getPipelineProgram(VertexType vertexType, StructType<?> structType, ContextShader contextShader, PipelineShader pipelineShader) {
		return pipelinePrograms.get(new PipelineContext(vertexType, structType, contextShader, pipelineShader));
	}

	public GlProgram getCullingProgram(StructType<?> structType) {
		return cullingPrograms.get(structType);
	}

	protected GlProgram compilePipelineContext(PipelineContext ctx) throws ShaderCompilationException {

		var glslVersion = ctx.pipelineShader()
				.glslVersion();

		var vertex = new ShaderContext(glslVersion, ShaderType.VERTEX, ctx.getVertexComponents());
		var fragment = new ShaderContext(glslVersion, ShaderType.FRAGMENT, ctx.getFragmentComponents());

		return ctx.contextShader()
				.factory()
				.create(new ProgramAssembler().attachShader(shaderCompiler.get(vertex))
						.attachShader(shaderCompiler.get(fragment))
						.link());
	}

	protected GlProgram compileComputeCuller(StructType<?> structType) {
		var location = structType.getInstanceShader();

		var finalSource = new StringBuilder();
		CompilationContext context = new CompilationContext();
		var components = List.of(new IndirectComponent(structType.getLayout().layoutItems), location.getFile(), Components.Pipeline.INDIRECT_CULL.getFile());

		var names = ImmutableList.<ResourceLocation>builder();
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : components) {
			included.addAll(component.included());
			names.add(component.name());
		}

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V460, ShaderType.COMPUTE));
		for (var include : included) {
			finalSource.append(include.source(context));
		}

		for (var component : components) {
			finalSource.append(component.source(context));
		}

		try {
			var fileLoc = location.getFileLoc();
			var shader = new GlShader(finalSource.toString(), ShaderType.COMPUTE, ImmutableList.of(fileLoc));

			var program = new ProgramAssembler().attachShader(shader)
					.link();
			return new GlProgram(program);
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(context);
		}
	}
}
