package com.jozufozu.flywheel.core.compile;

import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.Variable;

public class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {
	private final FileResolution contextShader;

	public FragmentCompiler(FileResolution contextShader) {
		this.contextShader = contextShader;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V150, ShaderType.FRAGMENT));

		key.getShaderConstants().writeInto(finalSource);
		finalSource.append('\n');

		FileIndexImpl index = new FileIndexImpl();

		//

		SourceFile materialShader = key.materialShader;

		Optional<ShaderFunction> maybeMaterialFragment = materialShader.findFunction("flw_materialFragment");

		if (maybeMaterialFragment.isEmpty()) {
			ErrorReporter.generateMissingFunction(materialShader, "flw_materialFragment", "\"flw_materialFragment\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction materialFragment = maybeMaterialFragment.get();
		ImmutableList<Variable> params = materialFragment.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(materialFragment.getArgs(), "\"flw_materialFragment\" function must not have any arguments");
			throw new ShaderLoadingException();
		}

		materialShader.generateFinalSource(index, finalSource);

		//

		SourceFile contextShaderSource = contextShader.getFile();

		Optional<ShaderFunction> maybeContextFragment = contextShaderSource.findFunction("flw_contextFragment");

		if (maybeContextFragment.isEmpty()) {
			ErrorReporter.generateMissingFunction(contextShaderSource, "flw_contextFragment", "\"flw_contextFragment\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction contextFragment = maybeContextFragment.get();
		params = contextFragment.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(contextFragment.getArgs(), "\"flw_contextFragment\" function must not have any arguments");
			throw new ShaderLoadingException();
		}

		contextShaderSource.generateFinalSource(index, finalSource);

		//

		finalSource.append(generateFooter());

		return new GlShader(contextShader.getFile().name, ShaderType.FRAGMENT, finalSource.toString());
	}

	protected String generateFooter() {
		StringBuilder footer = new StringBuilder();

		footer.append("""
				void main() {
					flw_materialFragment();

				    flw_contextFragment();
				}
				"""
		);

		return footer.toString();
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	/**
	 * Represents the conditions under which a shader is compiled.
	 */
	public static final class Context {
		/**
		 * The fragment material shader source.
		 */
		private final SourceFile materialShader;

		/**
		 * Alpha threshold below which fragments are discarded.
		 */
		private final float alphaDiscard;

		/**
		 * Which type of fog should be applied.
		 */
		private final FogType fogType;

		/**
		 * The shader constants to apply.
		 */
		private final StateSnapshot ctx;

		public Context(SourceFile materialShader, float alphaDiscard, FogType fogType, StateSnapshot ctx) {
			this.materialShader = materialShader;
			this.alphaDiscard = alphaDiscard;
			this.fogType = fogType;
			this.ctx = ctx;
		}

		public ShaderConstants getShaderConstants() {
			ShaderConstants shaderConstants = ctx.getShaderConstants();

			if (alphaDiscard > 0) {
				shaderConstants.define("ALPHA_DISCARD", alphaDiscard);
			}
			shaderConstants.define(fogType.name());

			return shaderConstants;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (Context) obj;
			return materialShader == that.materialShader && Objects.equals(this.ctx, that.ctx) && Float.floatToIntBits(this.alphaDiscard) == Float.floatToIntBits(that.alphaDiscard) && fogType == that.fogType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(materialShader, alphaDiscard, fogType, ctx);
		}

		@Override
		public String toString() {
			return "Context[" + "materialShader=" + materialShader + ", " + "alphaDiscard=" + alphaDiscard + ", " + "fogType=" + fogType + ", " + "ctx=" + ctx + ']';
		}
	}
}
