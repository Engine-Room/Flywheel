package com.jozufozu.flywheel.core.compile;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.parse.StructField;
import com.jozufozu.flywheel.core.source.parse.Variable;
import com.jozufozu.flywheel.core.source.span.Span;

public class FragmentTemplateData implements FragmentData {
	public final SourceFile file;
	public final Span interpolantName;
	public final ShaderStruct interpolant;
	public final ShaderFunction fragmentMain;

	public FragmentTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeFragmentMain = file.findFunction("fragment");

		if (maybeFragmentMain.isEmpty()) {
			ErrorReporter.generateMissingFunction(file, "fragment", "\"fragment\" function not defined");
			throw new RuntimeException();
		}

		fragmentMain = maybeFragmentMain.get();
		ImmutableList<Variable> fragmentParameters = fragmentMain.getParameters();


		if (fragmentParameters.size() != 1) {
			ErrorReporter.generateSpanError(fragmentMain.getArgs(), "fragment function must have exactly one argument");
			throw new RuntimeException();
		}

		interpolantName = fragmentMain.getParameters().get(0).type;

		Optional<ShaderStruct> maybeInterpolant = file.findStruct(interpolantName);

		if (maybeInterpolant.isEmpty()) {
			ErrorReporter.generateMissingStruct(file, interpolantName, "struct not defined");

			throw new RuntimeException();
		}

		interpolant = maybeInterpolant.get();
	}

	@Override
	public String generateFooter() {
		StringBuilder builder = new StringBuilder();
		prefixFields(builder, interpolant, "in", "v2f_");

		builder.append(String.format("""
				void main() {
				    Fragment o;
					o.color = v2f_color;
					o.texCoords = v2f_texCoords;
					o.light = v2f_light;
					o.diffuse = v2f_diffuse;

				    vec4 color = %s;
				    FLWFinalizeColor(color);
				}
				""",
				fragmentMain.call("o")
		));

		return builder.toString();
	}

	public static void prefixFields(StringBuilder builder, ShaderStruct struct, String qualifier, String prefix) {
		ImmutableList<StructField> fields = struct.getFields();

		for (StructField field : fields) {
			builder.append(qualifier)
					.append(' ')
					.append(field.type)
					.append(' ')
					.append(prefix)
					.append(field.name)
					.append(";\n");
		}
	}
}
