package com.jozufozu.flywheel.core.compile;

import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.shader.StateSnapshot;
import com.jozufozu.flywheel.core.source.FileIndexImpl;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.parse.StructField;
import com.jozufozu.flywheel.core.source.parse.Variable;
import com.jozufozu.flywheel.core.source.span.Span;

public class VertexCompiler extends Memoizer<VertexCompiler.Context, GlShader> {
	private final FileResolution contextShader;

	public VertexCompiler(FileResolution contextShader) {
		this.contextShader = contextShader;
	}

	@Override
	protected GlShader _create(Context key) {
		StringBuilder finalSource = new StringBuilder();

		finalSource.append(CompileUtil.generateHeader(GLSLVersion.V330, ShaderType.VERTEX));

		key.ctx.getShaderConstants().writeInto(finalSource);
		finalSource.append('\n');

		FileIndexImpl index = new FileIndexImpl();

		//

		SourceFile layoutShader = key.vertexType.getLayoutShader().getFile();

		Optional<ShaderFunction> maybeLayoutVertex = layoutShader.findFunction("flw_layoutVertex");

		if (maybeLayoutVertex.isEmpty()) {
			ErrorReporter.generateMissingFunction(layoutShader, "flw_layoutVertex", "\"flw_layoutVertex\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction layoutVertex = maybeLayoutVertex.get();
		ImmutableList<Variable> params = layoutVertex.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(layoutVertex.getArgs(), "\"flw_layoutVertex\" function must not have any arguments");
			throw new ShaderLoadingException();
		}

		layoutShader.generateFinalSource(index, finalSource);

		//

		SourceFile instanceShader = key.instanceShader;

		Optional<ShaderFunction> maybeInstanceVertex = instanceShader.findFunction("flw_instanceVertex");

		if (maybeInstanceVertex.isEmpty()) {
			ErrorReporter.generateMissingFunction(instanceShader, "flw_instanceVertex", "\"flw_instanceVertex\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction instanceVertex = maybeInstanceVertex.get();
		params = instanceVertex.getParameters();

		if (params.size() != 1) {
			ErrorReporter.generateSpanError(instanceVertex.getArgs(), "\"flw_instanceVertex\" function must have exactly 1 argument");
			throw new ShaderLoadingException();
		}

		Span instanceName = params.get(0).type;
		Optional<ShaderStruct> maybeInstance = instanceShader.findStruct(instanceName);

		if (maybeInstance.isEmpty()) {
			ErrorReporter.generateMissingStruct(instanceShader, instanceName, "instance struct not defined");
			throw new ShaderLoadingException();
		}

		ShaderStruct instance = maybeInstance.get();

		instanceShader.generateFinalSource(index, finalSource);

		//

		SourceFile materialShader = key.materialShader;

		Optional<ShaderFunction> maybeMaterialVertex = materialShader.findFunction("flw_materialVertex");

		if (maybeMaterialVertex.isEmpty()) {
			ErrorReporter.generateMissingFunction(materialShader, "flw_materialVertex", "\"flw_materialVertex\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction materialVertex = maybeMaterialVertex.get();
		params = materialVertex.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(materialVertex.getArgs(), "\"flw_materialVertex\" function must not have any arguments");
			throw new ShaderLoadingException();
		}

		materialShader.generateFinalSource(index, finalSource);

		//

		SourceFile contextShaderSource = contextShader.getFile();

		Optional<ShaderFunction> maybeContextVertex = contextShaderSource.findFunction("flw_contextVertex");

		if (maybeContextVertex.isEmpty()) {
			ErrorReporter.generateMissingFunction(contextShaderSource, "flw_contextVertex", "\"flw_contextVertex\" function not defined");
			throw new ShaderLoadingException();
		}

		ShaderFunction contextVertex = maybeContextVertex.get();
		params = contextVertex.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(contextVertex.getArgs(), "\"flw_contextVertex\" function must not have any arguments");
			throw new ShaderLoadingException();
		}

		contextShaderSource.generateFinalSource(index, finalSource);

		//

		finalSource.append(generateFooter(key.vertexType, instance));

		return new GlShader(instanceShader.name, ShaderType.VERTEX, finalSource.toString());
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
		 * The vertex material shader source.
		 */
		private final SourceFile materialShader;

		/**
		 * The shader constants to apply.
		 */
		private final StateSnapshot ctx;

		public Context(VertexType vertexType, SourceFile instanceShader, SourceFile materialShader, StateSnapshot ctx) {
			this.vertexType = vertexType;
			this.instanceShader = instanceShader;
			this.materialShader = materialShader;
			this.ctx = ctx;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (Context) o;
			return vertexType == that.vertexType && instanceShader == that.instanceShader && materialShader == that.materialShader && ctx.equals(that.ctx);
		}

		@Override
		public int hashCode() {
			return Objects.hash(vertexType, instanceShader, materialShader, ctx);
		}
	}
}
