package dev.engine_room.flywheel.backend.compile.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.lib.util.StringUtil;
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
	public ShaderCompiler<K> shader(GlslVersion glslVersion, ShaderType shaderType) {
		return new ShaderCompiler<>(glslVersion, shaderType);
	}

	public ProgramStitcher<K> program() {
		return new ProgramStitcher<>();
	}

	public static class ShaderCompiler<K> {
		private final GlslVersion glslVersion;
		private final ShaderType shaderType;
		private final List<BiFunction<K, ShaderSources, SourceComponent>> fetchers = new ArrayList<>();
		private BiConsumer<K, Compilation> compilationCallbacks = ($, $$) -> {
		};
		private Function<K, String> nameMapper = Object::toString;

		public ShaderCompiler(GlslVersion glslVersion, ShaderType shaderType) {
			this.glslVersion = glslVersion;
			this.shaderType = shaderType;
		}

		public ShaderCompiler<K> nameMapper(Function<K, String> nameMapper) {
			this.nameMapper = nameMapper;
			return this;
		}

		public ShaderCompiler<K> with(BiFunction<K, ShaderSources, SourceComponent> fetch) {
			fetchers.add(fetch);
			return this;
		}

		public ShaderCompiler<K> withComponents(Collection<SourceComponent> components) {
			components.forEach(this::withComponent);
			return this;
		}

		public ShaderCompiler<K> withComponent(SourceComponent component) {
			return withComponent($ -> component);
		}

		public ShaderCompiler<K> withComponent(Function<K, SourceComponent> sourceFetcher) {
			return with((key, $) -> sourceFetcher.apply(key));
		}

		public ShaderCompiler<K> withResource(Function<K, ResourceLocation> sourceFetcher) {
			return with((key, loader) -> loader.get(sourceFetcher.apply(key)));
		}

		public ShaderCompiler<K> withResource(ResourceLocation resourceLocation) {
			return withResource($ -> resourceLocation);
		}

		public ShaderCompiler<K> onCompile(BiConsumer<K, Compilation> cb) {
			compilationCallbacks = compilationCallbacks.andThen(cb);
			return this;
		}

		public ShaderCompiler<K> define(String def, int value) {
			return onCompile(($, ctx) -> ctx.define(def, String.valueOf(value)));
		}

		public ShaderCompiler<K> enableExtension(String extension) {
			return onCompile(($, ctx) -> ctx.enableExtension(extension));
		}

		public ShaderCompiler<K> enableExtensions(String... extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.enableExtension(extension);
				}
			});
		}

		public ShaderCompiler<K> enableExtensions(Collection<String> extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.enableExtension(extension);
				}
			});
		}

		public ShaderCompiler<K> requireExtensions(Collection<String> extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.requireExtension(extension);
				}
			});
		}

		private GlShader compile(K key, ShaderCache compiler, ShaderSources loader) {
			long start = System.nanoTime();

			var components = new ArrayList<SourceComponent>();
			for (var fetcher : fetchers) {
				components.add(fetcher.apply(key, loader));
			}

			Consumer<Compilation> cb = ctx -> compilationCallbacks.accept(key, ctx);
			var name = nameMapper.apply(key);
			var out = compiler.compile(glslVersion, shaderType, name, cb, components);

			long end = System.nanoTime();

			FlwPrograms.LOGGER.debug("Compiled {} in {}", name, StringUtil.formatTime(end - start));

			return out;
		}
	}

	public static class ProgramStitcher<K> implements CompilationHarness.KeyCompiler<K> {
		private final Map<ShaderType, ShaderCompiler<K>> compilers = new EnumMap<>(ShaderType.class);
		private BiConsumer<K, GlProgram> postLink = (k, p) -> {
		};
		private BiConsumer<K, GlProgram> preLink = (k, p) -> {
		};

		public CompilationHarness<K> harness(String marker, ShaderSources sources) {
			return new CompilationHarness<>(marker, sources, this);
		}

		public ProgramStitcher<K> link(ShaderCompiler<K> compilerBuilder) {
			if (compilers.containsKey(compilerBuilder.shaderType)) {
				throw new IllegalArgumentException("Duplicate shader type: " + compilerBuilder.shaderType);
			}
			compilers.put(compilerBuilder.shaderType, compilerBuilder);
			return this;
		}

		public ProgramStitcher<K> postLink(BiConsumer<K, GlProgram> postLink) {
			this.postLink = postLink;
			return this;
		}

		public ProgramStitcher<K> preLink(BiConsumer<K, GlProgram> preLink) {
			this.preLink = preLink;
			return this;
		}

		@Override
		public GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker) {
			if (compilers.isEmpty()) {
				throw new IllegalStateException("No shader compilers were added!");
			}

			long start = System.nanoTime();

			List<GlShader> shaders = new ArrayList<>();

			for (ShaderCompiler<K> compiler : compilers.values()) {
				shaders.add(compiler.compile(key, shaderCache, loader));
			}

			var out = programLinker.link(shaders, p -> preLink.accept(key, p));

			postLink.accept(key, out);

			long end = System.nanoTime();

			FlwPrograms.LOGGER.debug("Linked {} in {}", key, StringUtil.formatTime(end - start));

			return out;
		}
	}
}
