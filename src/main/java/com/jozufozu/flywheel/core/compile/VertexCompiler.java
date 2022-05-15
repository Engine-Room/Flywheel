package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;

public class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {
	private final Template<? extends VertexData> template;
	private final FileResolution contextShader;

	public VertexCompiler(Template<? extends VertexData> template, FileResolution contextShader) {
		this.template = template;
		this.contextShader = contextShader;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(template.getVersion(), ShaderType.VERTEX));

		key.ctx.getShaderConstants().writeInto(finalSource);
		finalSource.append('\n');

		FileIndexImpl index = new FileIndexImpl();

		FileResolution layoutShader = key.vertexType.getLayoutShader();
		layoutShader.getFile().generateFinalSource(index, finalSource);

		contextShader.getFile().generateFinalSource(index, finalSource);

		key.instanceShader.generateFinalSource(index, finalSource);

		VertexData appliedTemplate = template.apply(key.instanceShader);
		finalSource.append(appliedTemplate.generateFooter(index, key.vertexType));

		return new GlShader(key.instanceShader.name, ShaderType.VERTEX, finalSource.toString());
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	public static class Context {
		/**
		 * The vertex type to use.
		 */
		private final VertexType vertexType;

		/**
		 * The instance shader source.
		 */
		private final SourceFile instanceShader;

		/**
		 * The shader constants to apply.
		 */
		private final StateSnapshot ctx;

		public Context(VertexType vertexType, SourceFile instanceShader, StateSnapshot ctx) {
			this.vertexType = vertexType;
			this.instanceShader = instanceShader;
			this.ctx = ctx;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (Context) o;
			return vertexType == that.vertexType && instanceShader == that.instanceShader && ctx.equals(that.ctx);
		}

		@Override
		public int hashCode() {
			return Objects.hash(vertexType, instanceShader, ctx);
		}
	}
}
