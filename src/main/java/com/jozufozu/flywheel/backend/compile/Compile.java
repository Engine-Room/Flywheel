package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.compile.core.Compilation;
import com.jozufozu.flywheel.backend.compile.core.ProgramLinker;
import com.jozufozu.flywheel.backend.compile.core.ShaderCompiler;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;

import net.minecraft.resources.ResourceLocation;

/**
 * A typed provider for shader compiler builders.
 * <br>
 * This could just be a static utility class, but creating an instance of Compile
 * and calling the functors on it prevents you from having to specify the key type everywhere.
 * <br>
 * Consider {@code Compile.<PipelineKey>shader(...)} vs {@code PIPELINE.shader(...)}
 *
 * @param <K> The type of the key used to compile shaders.
 */
public class Compile<K> {
	public ShaderCompilerBuilder<K> shader(GLSLVersion glslVersion, ShaderType shaderType) {
		return new ShaderCompilerBuilder<>(glslVersion, shaderType);
	}

	public ProgramLinkBuilder<K> program() {
		return new ProgramLinkBuilder<>();
	}

	public CompilationHarness.Builder<K> harness(ShaderSources sources) {
		return new CompilationHarness.Builder<>(sources);
	}

	public static class ProgramLinkBuilder<K> implements CompilationHarness.KeyCompiler<K> {
		private final Map<ShaderType, ShaderCompilerBuilder<K>> compilers = new EnumMap<>(ShaderType.class);
		private BiConsumer<K, GlProgram> onLink = (k, p) -> {
		};

		public ProgramLinkBuilder<K> link(ShaderCompilerBuilder<K> compilerBuilder) {
			if (compilers.containsKey(compilerBuilder.shaderType)) {
				throw new IllegalArgumentException("Duplicate shader type: " + compilerBuilder.shaderType);
			}
			compilers.put(compilerBuilder.shaderType, compilerBuilder);
			return this;
		}

		public ProgramLinkBuilder<K> then(BiConsumer<K, GlProgram> onLink) {
			this.onLink = onLink;
			return this;
		}

		@Override
		@Nullable
		public GlProgram compile(K key, SourceLoader loader, ShaderCompiler shaderCompiler, ProgramLinker programLinker) {
			if (compilers.isEmpty()) {
				throw new IllegalStateException("No shader compilers were added!");
			}

			List<GlShader> shaders = new ArrayList<>();

			boolean ok = true;
			for (ShaderCompilerBuilder<K> compiler : compilers.values()) {
				var shader = compiler.compile(key, shaderCompiler, loader);
				if (shader == null) {
					ok = false;
				}
				shaders.add(shader);
			}

			if (!ok) {
				return null;
			}

			var out = programLinker.link(shaders);

			if (out != null) {
				onLink.accept(key, out);
			}

			return out;
		}
	}

	public static class ShaderCompilerBuilder<K> {
		private final GLSLVersion glslVersion;
		private final ShaderType shaderType;
		private Consumer<Compilation> compilationCallbacks = $ -> {
		};
		private final List<BiFunction<K, SourceLoader, SourceComponent>> fetchers = new ArrayList<>();

		public ShaderCompilerBuilder(GLSLVersion glslVersion, ShaderType shaderType) {
			this.glslVersion = glslVersion;
			this.shaderType = shaderType;
		}

		public ShaderCompilerBuilder<K> with(BiFunction<K, SourceLoader, SourceComponent> fetch) {
			fetchers.add(fetch);
			return this;
		}

		public ShaderCompilerBuilder<K> withComponent(SourceComponent component) {
			return withComponent($ -> component);
		}

		public ShaderCompilerBuilder<K> withComponent(Function<K, @NotNull SourceComponent> sourceFetcher) {
			return with((key, $) -> sourceFetcher.apply(key));
		}

		public ShaderCompilerBuilder<K> withResource(Function<K, @NotNull ResourceLocation> sourceFetcher) {
			return with((key, loader) -> loader.find(sourceFetcher.apply(key)));
		}

		public ShaderCompilerBuilder<K> withResource(ResourceLocation resourceLocation) {
			return withResource($ -> resourceLocation);
		}

		public ShaderCompilerBuilder<K> onCompile(Consumer<Compilation> cb) {
			compilationCallbacks = compilationCallbacks.andThen(cb);
			return this;
		}

		public ShaderCompilerBuilder<K> define(String def, int value) {
			return onCompile(ctx -> ctx.define(def, String.valueOf(value)));
		}

		public ShaderCompilerBuilder<K> enableExtension(String extension) {
			return onCompile(ctx -> ctx.enableExtension(extension));
		}

		@Nullable
		private GlShader compile(K key, ShaderCompiler compiler, SourceLoader loader) {
			var components = new ArrayList<SourceComponent>();
			boolean ok = true;
			for (var fetcher : fetchers) {
				SourceComponent apply = fetcher.apply(key, loader);
				if (apply == null) {
					ok = false;
				}
				components.add(apply);
			}

			if (!ok) {
				return null;
			}

			return compiler.compile(glslVersion, shaderType, compilationCallbacks, components);
		}
	}
}
