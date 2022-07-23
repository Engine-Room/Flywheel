package com.jozufozu.flywheel.core.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
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

		var shaderConstants = key.getShaderConstants();
		shaderConstants.writeInto(finalSource);
		finalSource.append('\n');

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
			return new GlShader(finalSource.toString(), ShaderType.FRAGMENT, ImmutableList.of(materialShader.name, contextShaderSource.name), shaderConstants);
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(ctx);
		}
	}

	protected String generateFooter() {
		return """
				void main() {
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
	 * @param materialShader The fragment material shader source.
	 * @param alphaDiscard Alpha threshold below which fragments are discarded.
	 * @param fogType Which type of fog should be applied.
	 * @param ctx The shader constants to apply.
	 */
	public record Context(SourceFile materialShader, SourceFile contextShader, float alphaDiscard, FogType fogType, StateSnapshot ctx) {

		public ShaderConstants getShaderConstants() {
			ShaderConstants shaderConstants = ctx.getShaderConstants();

			if (alphaDiscard > 0) {
				shaderConstants.define("ALPHA_DISCARD", alphaDiscard);
			}
			shaderConstants.define(fogType.name());

			return shaderConstants;
		}
	}
}
