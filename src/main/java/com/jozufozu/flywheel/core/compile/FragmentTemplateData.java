package com.jozufozu.flywheel.core.compile;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.Variable;

public class FragmentTemplateData implements FragmentData {
	public final SourceFile file;
	public final ShaderFunction contextFragment;

	public FragmentTemplateData(SourceFile file) {
		this.file = file;

		Optional<ShaderFunction> maybeContextFragment = file.findFunction("flw_contextFragment");

		if (maybeContextFragment.isEmpty()) {
			ErrorReporter.generateMissingFunction(file, "flw_contextFragment", "\"flw_contextFragment\" function not defined");
			throw new ShaderLoadingException();
		}

		contextFragment = maybeContextFragment.get();
		ImmutableList<Variable> params = contextFragment.getParameters();

		if (params.size() != 0) {
			ErrorReporter.generateSpanError(contextFragment.getArgs(), "\"flw_contextFragment\" function must not have any arguments");
			throw new ShaderLoadingException();
		}
	}

	@Override
	public String generateFooter() {
		StringBuilder builder = new StringBuilder();

		builder.append("""
				void main() {
				    flw_contextFragment();
				}
				"""
		);

		return builder.toString();
	}
}
