package com.jozufozu.flywheel.core.compile;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.parse.StructField;

/**
 * Handles compilation and deletion of vertex shaders.
 */
public class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {
	private final FileResolution contextShader;
	private final GLSLVersion glslVersion;

	public VertexCompiler(FileResolution contextShader, GLSLVersion glslVersion) {
		this.contextShader = contextShader;
		this.glslVersion = glslVersion;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(glslVersion, ShaderType.VERTEX));

		var shaderConstants = key.ctx.getShaderConstants();
		shaderConstants.writeInto(finalSource);
		finalSource.append('\n');

		var index = new FileIndexImpl();

		// LAYOUT

		var layoutShader = key.vertexType.getLayoutShader().getFile();
		layoutShader.generateFinalSource(index, finalSource);

		// INSTANCE

		var instanceShader = key.instanceShader;
		instanceShader.generateFinalSource(index, finalSource);

		// MATERIAL

		var materialShader = key.materialShader;
		materialShader.generateFinalSource(index, finalSource);

		// CONTEXT

		var contextShaderSource = contextShader.getFile();
		contextShaderSource.generateFinalSource(index, finalSource);

		// MAIN

		var instanceStruct = instanceShader.findFunction("flw_instanceVertex")
				.flatMap(f -> f.getParameterType(0)
						.findStruct())
				.orElseThrow();
		finalSource.append(generateFooter(key.vertexType, instanceStruct));

		return new GlShader(finalSource.toString(), ShaderType.VERTEX, ImmutableList.of(layoutShader.name, instanceShader.name, materialShader.name, contextShaderSource.name), shaderConstants);
	}

	protected String generateFooter(VertexType vertexType, ShaderStruct instance) {
		ImmutableList<StructField> fields = instance.getFields();

		int attributeBinding = vertexType.getLayout()
				.getAttributeCount();

		StringBuilder footer = new StringBuilder();

		for (StructField field : fields) {
			footer.append("layout(location = ")
					.append(attributeBinding)
					.append(") in")
					.append(' ')
					.append(field.type)
					.append(' ')
					.append("_flw_a_i_")
					.append(field.name)
					.append(";\n");
			attributeBinding += CompileUtil.getAttributeCount(field.type);
		}
		footer.append('\n');

		footer.append(String.format("""
						void main() {
							flw_layoutVertex();

							%s instance;
							%s
							flw_instanceVertex(instance);

							flw_materialVertex();

							flw_contextVertex();
						}
						""",
				instance.name,
				assignFields(instance, "instance.", "_flw_a_i_")
		));

		return footer.toString();
	}

	protected static StringBuilder assignFields(ShaderStruct struct, String prefix1, String prefix2) {
		ImmutableList<StructField> fields = struct.getFields();

		StringBuilder builder = new StringBuilder();

		for (StructField field : fields) {
			builder.append(prefix1)
					.append(field.name)
					.append(" = ")
					.append(prefix2)
					.append(field.name)
					.append(";\n");
		}

		return builder;
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	/**
	 * @param vertexType The vertex type to use.
	 * @param instanceShader The instance shader source.
	 * @param materialShader The vertex material shader source.
	 * @param ctx The shader constants to apply.
	 */
	public record Context(VertexType vertexType, SourceFile instanceShader, SourceFile materialShader, StateSnapshot ctx) {
	}
}
