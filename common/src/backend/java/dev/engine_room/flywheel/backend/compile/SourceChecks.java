package dev.engine_room.flywheel.backend.compile;

// TODO: recycle to be invoked by the shader compiler
public class SourceChecks {
	//	public static final BiConsumer<ErrorReporter, SourceFile> LAYOUT_VERTEX = checkFunctionArity("flw_layoutVertex", 0);
	//	public static final BiConsumer<ErrorReporter, SourceFile> INSTANCE_VERTEX = checkFunctionParameterTypeExists("flw_instanceVertex", 1, 0);
	//	public static final BiConsumer<ErrorReporter, SourceFile> MATERIAL_VERTEX = checkFunctionArity("flw_materialVertex", 0);
	//	public static final BiConsumer<ErrorReporter, SourceFile> MATERIAL_FRAGMENT = checkFunctionArity("flw_materialFragment", 0);
	//	public static final BiConsumer<ErrorReporter, SourceFile> CONTEXT_VERTEX = checkFunctionArity("flw_contextVertex", 0);
	//	public static final BiConsumer<ErrorReporter, SourceFile> CONTEXT_FRAGMENT = checkFunctionArity("flw_contextFragment", 0).andThen(checkFunctionArity("flw_initFragment", 0));
	//	public static final BiConsumer<ErrorReporter, SourceFile> PIPELINE = checkFunctionArity("main", 0);
	//
	//	public static BiConsumer<ErrorReporter, SourceFile> checkFunctionArity(String name, int arity) {
	//		return (errorReporter, file) -> checkFunctionArity(errorReporter, file, name, arity);
	//	}
	//
	//	public static BiConsumer<ErrorReporter, SourceFile> checkFunctionParameterTypeExists(String name, int arity, int param) {
	//		return (errorReporter, file) -> {
	//			var func = checkFunctionArity(errorReporter, file, name, arity);
	//
	//			if (func == null) {
	//				return;
	//			}
	//
	//			var maybeStruct = func.getParameterType(param)
	//					.findStruct();
	//
	//			if (maybeStruct.isEmpty()) {
	//				errorReporter.generateMissingStruct(file, func.getParameterType(param), "struct not defined");
	//			}
	//		};
	//	}
	//
	//	/**
	//	 * @return {@code null} if the function doesn't exist, or if the function has the wrong arity.
	//	 */
	//	@Nullable
	//	private static ShaderFunction checkFunctionArity(ErrorReporter errorReporter, SourceFile file, String name, int arity) {
	//		Optional<ShaderFunction> maybeFunc = file.findFunction(name);
	//
	//		if (maybeFunc.isEmpty()) {
	//			errorReporter.generateMissingFunction(file, name, "\"" + name + "\" function not defined");
	//			return null;
	//		}
	//
	//		ShaderFunction func = maybeFunc.get();
	//		ImmutableList<ShaderVariable> params = func.getParameters();
	//		if (params.size() != arity) {
	//			errorReporter.generateFunctionArgumentCountError(name, arity, func.getArgs());
	//			return null;
	//		}
	//
	//		return func;
	//	}
}
