package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.ProgramSpec;

public class VertexCompiler extends Memoizer<ProgramContext, GlShader> {
	private final Template<? extends VertexData> template;
	private final FileResolution header;

	public VertexCompiler(Template<? extends VertexData> template, FileResolution header) {
		this.template = template;
		this.header = header;
	}

	@Override
	protected GlShader _create(ProgramContext key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(template.getVersion(), ShaderType.VERTEX));

		key.getShaderConstants().writeInto(finalSource);

		finalSource.append("""
				struct Vertex {
					vec3 pos;
					vec4 color;
					vec2 texCoords;
					vec2 light;
					vec3 normal;
				};
				""");
		finalSource.append(key.vertexType()
				.getShaderHeader());

		FileIndexImpl index = new FileIndexImpl();

		header.getFile().generateFinalSource(index, finalSource);
		ProgramSpec spec = key.spec();
		SourceFile vertexFile = spec.getVertexFile();

		vertexFile.generateFinalSource(index, finalSource);

		VertexData appliedTemplate = template.apply(vertexFile);
		finalSource.append(appliedTemplate.generateFooter(index, key.vertexType()));

		return new GlShader(spec.name, ShaderType.VERTEX, finalSource.toString());
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}
}
