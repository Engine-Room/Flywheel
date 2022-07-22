package com.jozufozu.flywheel.core.compile;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndex;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderField;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.util.Pair;

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

		var index = new FileIndex();

		// LAYOUT

		var layoutShader = key.vertexType.getLayoutShader().getFile();
		layoutShader.generateFinalSource(index, finalSource);

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
		instanceShader.generateFinalSource(index, finalSource, replacements);

		// MATERIAL

		var materialShader = key.materialShader;
		materialShader.generateFinalSource(index, finalSource);

		// CONTEXT

		var contextShaderSource = contextShader.getFile();
		contextShaderSource.generateFinalSource(index, finalSource);

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
			return new GlShader(finalSource.toString(), ShaderType.VERTEX, ImmutableList.of(layoutShader.name, instanceShader.name, materialShader.name, contextShaderSource.name), shaderConstants);
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
	 * @param ctx The shader constants to apply.
	 */
	public record Context(VertexType vertexType, SourceFile instanceShader, SourceFile materialShader, StateSnapshot ctx) {
	}
}
