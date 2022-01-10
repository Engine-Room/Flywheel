package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.core.shader.ProgramSpec;

/**
 * Compiles a shader program.
 */
public class ShaderCompiler {

	public static <T extends VertexData> GlShader compileVertex(ProgramContext context, Template<T> template, FileResolution header) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(generateHeader(template.getVersion(), ShaderType.VERTEX));

		for (String def : context.createDefines()) {
			finalSource.append("#define ")
					.append(def)
					.append('\n');
		}

		finalSource.append("""
				struct Vertex {
					vec3 pos;
					vec4 color;
					vec2 texCoords;
					vec2 light;
					vec3 normal;
				};
				""");
		finalSource.append(context.vertexType()
				.getShaderHeader());

		FileIndexImpl index = new FileIndexImpl();

		header.getFile()
				.generateFinalSource(index, finalSource);
		ProgramSpec spec = context.spec();
		spec.getVertexFile()
				.generateFinalSource(index, finalSource);

		T appliedTemplate = template.apply(spec.getVertexFile());
		finalSource.append(appliedTemplate.generateFooter(index, context.vertexType()));

		return new GlShader(spec.name, ShaderType.VERTEX, finalSource.toString());
	}

	public static <T extends FragmentData> GlShader compileFragment(ProgramContext context, Template<T> template, FileResolution header) {

		StringBuilder finalSource = new StringBuilder();

		finalSource.append(generateHeader(template.getVersion(), ShaderType.FRAGMENT));
		for (String def : context.createDefines()) {
			finalSource.append("#define ")
					.append(def)
					.append('\n');
		}

		if (context.alphaDiscard() > 0) {
			finalSource.append("#define ALPHA_DISCARD 0.1\n");
		}


		FileIndexImpl index = new FileIndexImpl();

		ProgramSpec spec = context.spec();
		header.getFile().generateFinalSource(index, finalSource);
		spec.getFragmentFile()
				.generateFinalSource(index, finalSource);

		T appliedTemplate = template.apply(spec.getFragmentFile());
		finalSource.append(appliedTemplate.generateFooter());

		return new GlShader(spec.name, ShaderType.FRAGMENT, finalSource.toString());
	}

	protected static String generateHeader(GLSLVersion version, ShaderType type) {
		return "#version "
				+ version
				+ '\n'
				+ "#extension GL_ARB_explicit_attrib_location : enable\n"
				+ "#extension GL_ARB_conservative_depth : enable\n"
				+ type.getDefineStatement();
	}
}
