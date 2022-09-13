package com.jozufozu.flywheel.backend.instancing;

import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.compile.CompileUtil;
import com.jozufozu.flywheel.core.compile.Memoizer;
import com.jozufozu.flywheel.core.compile.ProgramAssembler;
import com.jozufozu.flywheel.core.compile.ShaderCompilationException;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;

import net.minecraft.resources.ResourceLocation;

/**
 * A caching compiler.
 *
 * <p>
 *     This class is responsible for compiling programs on the fly. An instance of this class will keep a cache of
 *     compiled programs, and will only compile a program if it is not already in the cache.
 * </p>
 * <p>
 *     A ProgramCompiler is also responsible for deleting programs and shaders on renderer reload.
 * </p>
 */
public class PipelineCompiler extends Memoizer<PipelineCompiler.Context, GlProgram> {

	public static final PipelineCompiler INSTANCE = new PipelineCompiler();

	private final ShaderCompiler shaderCompiler;

	private PipelineCompiler() {
		this.shaderCompiler = new ShaderCompiler();
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public GlProgram getProgram(PipelineCompiler.Context ctx) {
		return super.get(ctx);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		shaderCompiler.invalidate();
	}

	@Override
	protected GlProgram _create(PipelineCompiler.Context ctx) {

		var glslVersion = ctx.pipelineShader()
				.glslVersion();

		var vertex = new ShaderCompiler.Context(glslVersion, ShaderType.VERTEX, ctx.getVertexComponents());
		var fragment = new ShaderCompiler.Context(glslVersion, ShaderType.FRAGMENT, ctx.getFragmentComponents());

		return new ProgramAssembler(ctx.structType.getInstanceShader()
				.getFileLoc()).attachShader(shaderCompiler.get(vertex))
				.attachShader(shaderCompiler.get(fragment))
				.link()
				.build(ctx.contextShader.factory());
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		INSTANCE.invalidate();
	}

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param vertexType    The vertexType the program should be adapted for.
	 * @param material      The material shader to use. TODO: Flatten materials
	 * @param structType    The instance shader to use.
	 * @param contextShader The context shader to use.
	 */
	public record Context(VertexType vertexType, Material material, StructType<?> structType,
						  ContextShader contextShader, PipelineShader pipelineShader) {

		ImmutableList<SourceComponent> getVertexComponents() {
			var layout = vertexType.getLayoutShader()
					.getFile();
			var instanceAssembly = pipelineShader.assemble(vertexType, structType);
			var instance = structType.getInstanceShader()
					.getFile();
			var material = this.material.getVertexShader()
					.getFile();
			var context = contextShader.getVertexShader();
			var pipeline = pipelineShader.vertex()
					.getFile();
			return ImmutableList.of(layout, instanceAssembly, instance, material, context, pipeline);
		}

		ImmutableList<SourceComponent> getFragmentComponents() {
			var material = this.material.getFragmentShader()
					.getFile();
			var context = contextShader.getFragmentShader();
			var pipeline = pipelineShader.fragment()
					.getFile();
			return ImmutableList.of(material, context, pipeline);
		}
	}

	/**
	 * Handles compilation and deletion of vertex shaders.
	 */
	public static class ShaderCompiler extends Memoizer<ShaderCompiler.Context, GlShader> {

		private ShaderCompiler() {
		}

		@Override
		protected GlShader _create(Context key) {
			StringBuilder finalSource = new StringBuilder(key.generateHeader());
			finalSource.append("#extension GL_ARB_explicit_attrib_location : enable\n");
			finalSource.append("#extension GL_ARB_conservative_depth : enable\n");

			var ctx = new CompilationContext();

			var names = ImmutableList.<ResourceLocation>builder();
			var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
			for (var component : key.sourceComponents) {
				included.addAll(component.included());
				names.add(component.name());
			}

			for (var include : included) {
				finalSource.append(include.source(ctx));
			}

			for (var component : key.sourceComponents) {
				finalSource.append(component.source(ctx));
			}

			try {
				return new GlShader(finalSource.toString(), key.shaderType, names.build());
			} catch (ShaderCompilationException e) {
				throw e.withErrorLog(ctx);
			}
		}

		@Override
		protected void _destroy(GlShader value) {
			value.delete();
		}

		/**
		 * @param glslVersion      The GLSL version to use.
		 * @param sourceComponents A list of shader components to stitch together, in order.
		 */
		public record Context(GLSLVersion glslVersion, ShaderType shaderType, List<SourceComponent> sourceComponents) {

			public String generateHeader() {
				return CompileUtil.generateHeader(glslVersion, shaderType);
			}
		}

	}
}
