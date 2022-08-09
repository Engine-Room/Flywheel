package com.jozufozu.flywheel.backend.instancing;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;
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

		return new ProgramAssembler(ctx.instanceShader.getFileLoc())
				.attachShader(shaderCompiler.get(vertex))
				.attachShader(shaderCompiler.get(fragment))
				.link()
				.build(ctx.contextShader.factory());
	}

	@Override
	protected void _destroy(GlProgram value) {
		value.delete();
	}

	public static void invalidateAll(ReloadRenderersEvent ignored) {
		INSTANCE.invalidate();
	}

	/**
	 * Represents the entire context of a program's usage.
	 *
	 * @param vertexType     The vertexType the program should be adapted for.
	 * @param material       The material shader to use.
	 * @param instanceShader The instance shader to use.
	 * @param contextShader  The context shader to use.
	 */
	public record Context(VertexType vertexType, Material material, FileResolution instanceShader,
						  ContextShader contextShader, PipelineShader pipelineShader) {

		ImmutableList<SourceFile> getVertexComponents() {
			return ImmutableList.of(vertexType.getLayoutShader().getFile(), instanceShader.getFile(), material.getVertexShader().getFile(),
					contextShader.getVertexShader(), pipelineShader.vertex().getFile());
		}

		ImmutableList<SourceFile> getFragmentComponents() {
			return ImmutableList.of(material.getFragmentShader().getFile(), contextShader.getFragmentShader(),
					pipelineShader.fragment().getFile());
		}
	}

	/**
	 * Handles compilation and deletion of vertex shaders.
	 */
	public static class ShaderCompiler extends Memoizer<ShaderCompiler.Context, GlShader> {

		public ShaderCompiler() {
		}

		@Override
		protected GlShader _create(Context key) {
			StringBuilder finalSource = new StringBuilder();

			finalSource.append(key.generateHeader());
			finalSource.append("#extension GL_ARB_explicit_attrib_location : enable\n");
			finalSource.append("#extension GL_ARB_conservative_depth : enable\n");
			finalSource.append("#extension GL_ARB_enhanced_layouts : enable\n");

			var ctx = new CompilationContext();

			var names = ImmutableList.<ResourceLocation>builder();
			for (var file : key.components) {
				finalSource.append(file.generateFinalSource(ctx));
				names.add(file.name);
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
		 * @param glslVersion The GLSL version to use.
		 * @param components A list of shader components to stitch together, in order.
		 */
		public record Context(GLSLVersion glslVersion, ShaderType shaderType, List<SourceFile> components) {

			public String generateHeader() {
				return CompileUtil.generateHeader(glslVersion, shaderType);
			}
		}
	}
}
