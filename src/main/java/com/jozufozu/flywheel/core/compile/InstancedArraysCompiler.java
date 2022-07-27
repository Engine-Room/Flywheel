package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderField;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.util.Pair;

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
public class InstancedArraysCompiler extends Memoizer<InstancedArraysCompiler.Context, GlProgram> {

	public static final InstancedArraysCompiler INSTANCE = new InstancedArraysCompiler();

	private final VertexCompiler vertexCompiler;
	private final FragmentCompiler fragmentCompiler;

	private InstancedArraysCompiler() {
		this.vertexCompiler = new VertexCompiler();
		this.fragmentCompiler = new FragmentCompiler();
	}

	/**
	 * Get or compile a spec to the given vertex type, accounting for all game state conditions specified by the spec.
	 *
	 * @param ctx The context of compilation.
	 * @return A compiled GlProgram.
	 */
	public GlProgram getProgram(InstancedArraysCompiler.Context ctx) {
		return super.get(ctx);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		vertexCompiler.invalidate();
		fragmentCompiler.invalidate();
	}

	@Override
	protected GlProgram _create(InstancedArraysCompiler.Context ctx) {
		// TODO: try-catch here to prevent crashing if shaders failed to compile
		Material material = ctx.material;
		FileResolution instanceShader = ctx.instanceShader();
		ContextShader contextShader = ctx.contextShader;

		var vertex = new VertexCompiler.Context(ctx.vertexType(), instanceShader.getFile(), material.getVertexShader().getFile(),
				contextShader.getVertexShader());

		var fragment = new FragmentCompiler.Context(material.getFragmentShader().getFile(), contextShader.getFragmentShader());

		return new ProgramAssembler(instanceShader.getFileLoc())
				.attachShader(vertexCompiler.get(vertex))
				.attachShader(fragmentCompiler.get(fragment))
				.link()
				.build(contextShader.factory());
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
						  ContextShader contextShader) {
	}

	/**
	 * Handles compilation and deletion of vertex shaders.
	 */
	public static class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {

		public VertexCompiler() {
		}

		@Override
		protected GlShader _create(Context key) {
			StringBuilder finalSource = new StringBuilder();

			finalSource.append(CompileUtil.generateHeader(GLSLVersion.V420, ShaderType.VERTEX));

			var index = new CompilationContext();

			// LAYOUT

			var layoutShader = key.vertexType.getLayoutShader().getFile();
			finalSource.append(layoutShader.generateFinalSource(index));

			// INSTANCE

			int attributeBaseIndex = key.vertexType.getLayout()
					.getAttributeCount();

			var instanceShader = key.instanceShader;
			var replacements = new ArrayList<Pair<Span, String>>();
			for (ShaderField field : instanceShader.fields.values()) {
				if (field.decoration != ShaderField.Decoration.IN) {
					continue;
				}

				int location = Integer.parseInt(field.location.get());
				int newLocation = location + attributeBaseIndex;
				replacements.add(Pair.of(field.location, Integer.toString(newLocation)));
			}
			finalSource.append(instanceShader.generateFinalSource(index, replacements));

			// MATERIAL

			var materialShader = key.materialShader;
			finalSource.append(materialShader.generateFinalSource(index));

			// CONTEXT

			var contextShaderSource = key.contextShader;
			finalSource.append(contextShaderSource.generateFinalSource(index));

			// MAIN

			finalSource.append("""
					void main() {
						flw_layoutVertex();

						flw_instanceVertex();

						flw_materialVertex();

						flw_contextVertex();
					}
					""");

			try {
				return new GlShader(finalSource.toString(), ShaderType.VERTEX, ImmutableList.of(layoutShader.name, instanceShader.name, materialShader.name, contextShaderSource.name));
			} catch (ShaderCompilationException e) {
				throw e.withErrorLog(index);
			}
		}

		@Override
		protected void _destroy(GlShader value) {
			value.delete();
		}

		/**
		 * @param vertexType The vertex type to use.
		 * @param instanceShader The instance shader source.
		 * @param materialShader The vertex material shader source.
		 * @param contextShader The context shader source.
		 */
		public record Context(VertexType vertexType, SourceFile instanceShader, SourceFile materialShader, SourceFile contextShader) {
		}
	}

	/**
	 * Handles compilation and deletion of fragment shaders.
	 */
	public static class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {

		public FragmentCompiler() {
		}

		@Override
		protected GlShader _create(Context key) {
			StringBuilder finalSource = new StringBuilder();

			finalSource.append(CompileUtil.generateHeader(GLSLVersion.V420, ShaderType.FRAGMENT));

			var ctx = new CompilationContext();

			// MATERIAL

			SourceFile materialShader = key.materialShader;
			finalSource.append(materialShader.generateFinalSource(ctx));

			// CONTEXT

			SourceFile contextShaderSource = key.contextShader;
			finalSource.append(contextShaderSource.generateFinalSource(ctx));

			// MAIN

			finalSource.append(generateFooter());

			try {
				return new GlShader(finalSource.toString(), ShaderType.FRAGMENT, ImmutableList.of(materialShader.name, contextShaderSource.name));
			} catch (ShaderCompilationException e) {
				throw e.withErrorLog(ctx);
			}
		}

		protected String generateFooter() {
			return """
					void main() {
						flw_initFragment();

						flw_materialFragment();

						flw_contextFragment();
					}
					""";
		}

		@Override
		protected void _destroy(GlShader value) {
			value.delete();
		}

		/**
		 * Represents the conditions under which a shader is compiled.
		 *
		 * @param materialShader The fragment material shader source.
		 */
		public record Context(SourceFile materialShader, SourceFile contextShader) {

		}
	}
}
