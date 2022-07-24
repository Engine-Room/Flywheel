package com.jozufozu.flywheel.core.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.SourceFile;

/**
 * Handles compilation and deletion of fragment shaders.
 */
public class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {

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
