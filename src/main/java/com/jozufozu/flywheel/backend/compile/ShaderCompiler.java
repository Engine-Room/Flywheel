package com.jozufozu.flywheel.backend.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.gl.GLSLVersion;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.util.FlwUtil;

public class ShaderCompiler {
	private final Map<ShaderKey, CompilationResult> shaderCache = new HashMap<>();
	private final Consumer<FailedCompilation> errorConsumer;
	private final CompilationFactory factory;
	private final Includer includer;

	public ShaderCompiler(Consumer<FailedCompilation> errorConsumer, CompilationFactory factory, Includer includer) {
		this.errorConsumer = errorConsumer;
		this.factory = factory;
		this.includer = includer;
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

		CompilationResult out = compileUncached(factory.create(glslVersion, shaderType), sourceComponents);
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
	private CompilationResult compileUncached(Compilation ctx, ImmutableList<SourceComponent> sourceComponents) {
		ctx.enableExtension("GL_ARB_explicit_attrib_location");
		ctx.enableExtension("GL_ARB_conservative_depth");

		includer.expand(sourceComponents, ctx::appendComponent);

		return ctx.compile();
	}

	private record ShaderKey(GLSLVersion glslVersion, ShaderType shaderType,
							 ImmutableList<SourceComponent> sourceComponents) {

	}

	public static Builder builder() {
		return new Builder();
	}

	@FunctionalInterface
	public interface CompilationFactory {
		Compilation create(GLSLVersion version, ShaderType shaderType);
	}

	public static class Builder {
		private Consumer<FailedCompilation> errorConsumer = FlwUtil::noop;
		private CompilationFactory factory = Compilation::new;
		private Includer includer = RecursiveIncluder.INSTANCE;

		public Builder errorConsumer(Consumer<FailedCompilation> errorConsumer) {
			this.errorConsumer = errorConsumer;
			return this;
		}

		public Builder compilationFactory(CompilationFactory factory) {
			this.factory = factory;
			return this;
		}

		public Builder includer(Includer includer) {
			this.includer = includer;
			return this;
		}

		public ShaderCompiler build() {
			return new ShaderCompiler(errorConsumer, factory, includer);
		}
	}
}
