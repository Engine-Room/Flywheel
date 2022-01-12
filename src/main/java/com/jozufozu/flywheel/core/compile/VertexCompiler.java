package com.jozufozu.flywheel.core.compile;

import java.util.Objects;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.StateSnapshot;

public class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {
	private final Template<? extends VertexData> template;
	private final FileResolution header;

	public VertexCompiler(Template<? extends VertexData> template, FileResolution header) {
		this.template = template;
		this.header = header;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(template.getVersion(), ShaderType.VERTEX));

		key.ctx.getDefines().writeInto(finalSource);

		finalSource.append("""
				struct Vertex {
					vec3 pos;
					vec4 color;
					vec2 texCoords;
					vec2 light;
					vec3 normal;
				};
				""");
		finalSource.append(key.vertexType.getShaderHeader());

		FileIndexImpl index = new FileIndexImpl();

		header.getFile().generateFinalSource(index, finalSource);

		key.file.generateFinalSource(index, finalSource);

		VertexData appliedTemplate = template.apply(key.file);
		finalSource.append(appliedTemplate.generateFooter(index, key.vertexType));

		return new GlShader(key.file.name, ShaderType.VERTEX, finalSource.toString());
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	public static class Context {
		private final SourceFile file;
		private final StateSnapshot ctx;
		private final VertexType vertexType;

		public Context(SourceFile file, StateSnapshot ctx, VertexType vertexType) {
			this.file = file;
			this.ctx = ctx;
			this.vertexType = vertexType;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (Context) o;
			return file == that.file && vertexType == that.vertexType && ctx.equals(that.ctx);
		}

		@Override
		public int hashCode() {
			return Objects.hash(file, ctx, vertexType);
		}
	}
}
