package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.core.SourceComponent;

public class ShaderCompiler {
	private final Map<ShaderKey, CompilationResult> shaderCache = new HashMap<>();
	private final Consumer<FailedCompilation> errorConsumer;

	public ShaderCompiler(Consumer<FailedCompilation> errorConsumer) {
		this.errorConsumer = errorConsumer;
	}

	public int shaderCount() {
		return shaderCache.size();
	}

	@Nullable
	public GlShader compile(GLSLVersion glslVersion, ShaderType shaderType, ImmutableList<SourceComponent> sourceComponents) {
		var key = new ShaderKey(glslVersion, shaderType, sourceComponents);
		var cached = shaderCache.get(key);
		if (cached != null) {
			return cached.unwrap();
		}

		CompilationResult out = compileUncached(glslVersion, shaderType, sourceComponents);
		shaderCache.put(key, out);
		return unwrapAndReportError(out);
	}

	public void delete() {
		shaderCache.values()
				.stream()
				.map(CompilationResult::unwrap)
				.filter(Objects::nonNull)
				.forEach(GlShader::delete);
	}

	@Nullable
	private GlShader unwrapAndReportError(CompilationResult result) {
		if (result instanceof CompilationResult.Success s) {
			return s.shader();
		} else if (result instanceof CompilationResult.Failure f) {
			errorConsumer.accept(f.failure());
		}
		return null;
	}

	@NotNull
	private static CompilationResult compileUncached(GLSLVersion glslVersion, ShaderType shaderType, ImmutableList<SourceComponent> sourceComponents) {
		var ctx = new Compilation(glslVersion, shaderType);
		ctx.enableExtension("GL_ARB_explicit_attrib_location");
		ctx.enableExtension("GL_ARB_conservative_depth");

		for (var include : depthFirstInclude(sourceComponents)) {
			ctx.appendComponent(include);
		}

		for (var component : sourceComponents) {
			ctx.appendComponent(component);
			ctx.addComponentName(component.name());
		}

		return ctx.compile();
	}

	private static Set<SourceComponent> depthFirstInclude(ImmutableList<SourceComponent> root) {
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : root) {
			recursiveDepthFirstInclude(included, component);
		}
		return included;
	}

	private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
		for (var include : component.included()) {
			recursiveDepthFirstInclude(included, include);
		}
		included.addAll(component.included());
	}

	private record ShaderKey(GLSLVersion glslVersion, ShaderType shaderType,
							 ImmutableList<SourceComponent> sourceComponents) {

	}
}
