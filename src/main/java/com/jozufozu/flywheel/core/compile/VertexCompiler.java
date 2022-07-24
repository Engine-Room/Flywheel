package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.parse.ShaderField;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.Pair;

/**
 * Handles compilation and deletion of vertex shaders.
 */
public class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {

	public VertexCompiler() {
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V420, ShaderType.VERTEX));

		var index = new CompilationContext();

		// LAYOUT

		var layoutShader = key.vertexType.getLayoutShader().getFile();
		finalSource.append(layoutShader.generateFinalSource(index));

		// INSTANCE

		int attributeBaseIndex = key.vertexType.getLayout()
				.getAttributeCount();

		var instanceShader = key.instanceShader;
		var replacements = new ArrayList<Pair<Span, String>>();
		for (ShaderField field : instanceShader.fields.values()) {
			if (field.decoration != ShaderField.Decoration.IN) {
				continue;
			}

			int location = Integer.parseInt(field.location.get());
			int newLocation = location + attributeBaseIndex;
			replacements.add(Pair.of(field.location, Integer.toString(newLocation)));
		}
		finalSource.append(instanceShader.generateFinalSource(index, replacements));

		// MATERIAL

		var materialShader = key.materialShader;
		finalSource.append(materialShader.generateFinalSource(index));

		// CONTEXT

		var contextShaderSource = key.contextShader;
		finalSource.append(contextShaderSource.generateFinalSource(index));

		// MAIN

		finalSource.append("""
				void main() {
					flw_layoutVertex();

					flw_instanceVertex();

					flw_materialVertex();

					flw_contextVertex();
				}
				""");

		try {
			return new GlShader(finalSource.toString(), ShaderType.VERTEX, ImmutableList.of(layoutShader.name, instanceShader.name, materialShader.name, contextShaderSource.name));
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(index);
		}
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}

	/**
	 * @param vertexType The vertex type to use.
	 * @param instanceShader The instance shader source.
	 * @param materialShader The vertex material shader source.
	 * @param contextShader The context shader source.
	 */
	public record Context(VertexType vertexType, SourceFile instanceShader, SourceFile materialShader, SourceFile contextShader) {
	}
}
