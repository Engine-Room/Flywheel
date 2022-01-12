package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.StateSnapshot;

public class FragmentCompiler extends Memoizer<FragmentCompiler.Context, GlShader> {
	private final FileResolution header;
	private final Template<FragmentTemplateData> fragment;

	public FragmentCompiler(Template<FragmentTemplateData> fragment, FileResolution header) {
		this.header = header;
		this.fragment = fragment;
	}

	@Override
	protected GlShader _create(Context key) {
		SourceFile fragmentFile = key.file;
		FragmentTemplateData appliedTemplate = fragment.apply(fragmentFile);

		StringBuilder builder = new StringBuilder();

		builder.append(CompileUtil.generateHeader(fragment.getVersion(), ShaderType.FRAGMENT));

		key.getShaderConstants().writeInto(builder);

		FileIndexImpl index = new FileIndexImpl();

		header.getFile().generateFinalSource(index, builder);
		fragmentFile.generateFinalSource(index, builder);

		builder.append(appliedTemplate.generateFooter());

		return new GlShader(fragmentFile.name, ShaderType.FRAGMENT, builder.toString());
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	public static final class Context {
		private final SourceFile file;
		private final StateSnapshot ctx;
		private final float alphaDiscard;

		public Context(SourceFile file, StateSnapshot ctx, float alphaDiscard) {
			this.file = file;
			this.ctx = ctx;
			this.alphaDiscard = alphaDiscard;
		}

		public ShaderConstants getShaderConstants() {
			ShaderConstants shaderConstants = ctx.getDefines();

			if (alphaDiscard > 0) {
				shaderConstants.define("ALPHA_DISCARD", alphaDiscard);
			}

			return shaderConstants;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (Context) obj;
			return this.file == that.file && Objects.equals(this.ctx, that.ctx) && Float.floatToIntBits(this.alphaDiscard) == Float.floatToIntBits(that.alphaDiscard);
		}

		@Override
		public int hashCode() {
			return Objects.hash(file, ctx, alphaDiscard);
		}

		@Override
		public String toString() {
			return "Context[" + "file=" + file + ", " + "ctx=" + ctx + ", " + "alphaDiscard=" + alphaDiscard + ']';
		}

	}
}
