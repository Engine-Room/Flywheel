package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.CoreShaderInfoMap.CoreShaderInfo.FogType;
import com.jozufozu.flywheel.core.shader.ShaderConstants;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;

public class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {
	private final Template<? extends FragmentData> template;
	private final FileResolution header;

	public FragmentCompiler(Template<? extends FragmentData> template, FileResolution header) {
		this.header = header;
		this.template = template;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(template.getVersion(), ShaderType.FRAGMENT));

		key.getShaderConstants().writeInto(finalSource);

		FileIndexImpl index = new FileIndexImpl();

		header.getFile().generateFinalSource(index, finalSource);
		key.file.generateFinalSource(index, finalSource);

		FragmentData appliedTemplate = template.apply(key.file);
		finalSource.append(appliedTemplate.generateFooter());

		return new GlShader(key.file.name, ShaderType.FRAGMENT, finalSource.toString());
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
		 * The file to compile.
		 */
		private final SourceFile file;

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

		public Context(SourceFile file, StateSnapshot ctx, float alphaDiscard, FogType fogType) {
			this.file = file;
			this.ctx = ctx;
			this.alphaDiscard = alphaDiscard;
			this.fogType = fogType;
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
			return this.file == that.file && Objects.equals(this.ctx, that.ctx) && Float.floatToIntBits(this.alphaDiscard) == Float.floatToIntBits(that.alphaDiscard) && fogType == that.fogType;
		}

		@Override
		public int hashCode() {
			return Objects.hash(file, ctx, alphaDiscard, fogType);
		}

		@Override
		public String toString() {
			return "Context[" + "file=" + file + ", " + "ctx=" + ctx + ", " + "alphaDiscard=" + alphaDiscard + ", " + "fogType=" + fogType + ']';
		}

	}
}
