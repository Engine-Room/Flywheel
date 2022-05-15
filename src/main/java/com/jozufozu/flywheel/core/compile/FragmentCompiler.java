package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;

public class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {
	private final Template<? extends FragmentData> template;
	private final FileResolution contextShader;

	public FragmentCompiler(Template<? extends FragmentData> template, FileResolution contextShader) {
		this.template = template;
		this.contextShader = contextShader;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(template.getVersion(), ShaderType.FRAGMENT));

		key.getShaderConstants().writeInto(finalSource);
		finalSource.append('\n');

		FileIndexImpl index = new FileIndexImpl();

		contextShader.getFile().generateFinalSource(index, finalSource);

		FragmentData appliedTemplate = template.apply(contextShader.getFile());
		finalSource.append(appliedTemplate.generateFooter());

		return new GlShader(contextShader.getFile().name, ShaderType.FRAGMENT, finalSource.toString());
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
		 * The shader constants to apply.
		 */
		private final StateSnapshot ctx;

		/**
		 * Alpha threshold below which fragments are discarded.
		 */
		private final float alphaDiscard;

		/**
		 * Which type of fog should be applied.
		 */
		private final FogType fogType;

		public Context(float alphaDiscard, FogType fogType, StateSnapshot ctx) {
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
			return Objects.equals(this.ctx, that.ctx) && Float.floatToIntBits(this.alphaDiscard) == Float.floatToIntBits(that.alphaDiscard) && fogType == that.fogType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(alphaDiscard, fogType, ctx);
		}

		@Override
		public String toString() {
			return "Context[" + "alphaDiscard=" + alphaDiscard + ", " + "fogType=" + fogType + ", " + "ctx=" + ctx + ']';
		}

	}
}
