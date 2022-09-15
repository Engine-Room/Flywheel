package com.jozufozu.flywheel.core.source;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderVariable;

public class SourceChecks {

	public static BiConsumer<ErrorReporter, SourceFile> checkFunctionArity(String name, int arity) {
		return (errorReporter, file) -> checkFunctionArity(errorReporter, file, name, arity);
	}

	public static BiConsumer<ErrorReporter, SourceFile> checkFunctionParameterTypeExists(String name, int arity, int param) {
		return (errorReporter, file) -> {
			var func = checkFunctionArity(errorReporter, file, name, arity);

			if (func == null) {
				return;
			}

			var maybeStruct = func.getParameterType(param)
					.findStruct();

			if (maybeStruct.isEmpty()) {
				errorReporter.generateMissingStruct(file, func.getParameterType(param), "struct not defined");
			}
		};
	}

	/**
	 * @return {@code null} if the function doesn't exist, or if the function has the wrong arity.
	 */
	@Nullable
	private static ShaderFunction checkFunctionArity(ErrorReporter errorReporter, SourceFile file, String name, int arity) {
		Optional<ShaderFunction> maybeFunc = file.findFunction(name);

		if (maybeFunc.isEmpty()) {
			errorReporter.generateMissingFunction(file, name, "\"" + name + "\" function not defined");
			return null;
		}

		ShaderFunction func = maybeFunc.get();
		ImmutableList<ShaderVariable> params = func.getParameters();
		if (params.size() != arity) {
			errorReporter.generateFunctionArgumentCountError(name, arity, func.getArgs());
			return null;
		}

		return func;
	}
}
