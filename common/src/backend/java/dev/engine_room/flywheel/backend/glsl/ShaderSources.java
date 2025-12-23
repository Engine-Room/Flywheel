package dev.engine_room.flywheel.backend.glsl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.VisibleForTesting;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.lib.util.StringUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The main object for loading and parsing source files.
 */
public class ShaderSources {
	public static final String SHADER_DIR = "flywheel/";

	@VisibleForTesting
	protected final Map<Identifier, LoadResult> cache;

	public ShaderSources(ResourceManager manager) {
		var sourceFinder = new SourceFinder(manager);

		long loadStart = System.nanoTime();
		manager.listResources("flywheel", ShaderSources::isShader)
				.forEach(sourceFinder::rootLoad);

		long loadEnd = System.nanoTime();

		FlwPrograms.LOGGER.info("Loaded {} shader sources in {}", sourceFinder.results.size(), StringUtil.formatTime(loadEnd - loadStart));

		this.cache = sourceFinder.results;
	}

	private static Identifier locationWithoutFlywheelPrefix(Identifier id) {
		return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath()
				.substring(SHADER_DIR.length()));
	}

	public LoadResult find(Identifier id) {
		return cache.computeIfAbsent(id, loc -> new LoadResult.Failure(new LoadError.ResourceError(loc)));
	}

	public SourceFile get(Identifier id) {
		return find(id).unwrap();
	}

	private static boolean isShader(Identifier id) {
		var path = id.getPath();
		return path.endsWith(".glsl") || path.endsWith(".vert") || path.endsWith(".frag") || path.endsWith(".comp");
	}

	private static class SourceFinder {
		private final Deque<Identifier> findStack = new ArrayDeque<>();
		private final Map<Identifier, LoadResult> results = new HashMap<>();
		private final ResourceManager manager;

		public SourceFinder(ResourceManager manager) {
			this.manager = manager;
		}

		public void rootLoad(Identifier id, Resource resource) {
			var strippedId = locationWithoutFlywheelPrefix(id);

			if (results.containsKey(strippedId)) {
				// Some other source already #included this one.
				return;
			}

			this.results.put(strippedId, readResource(strippedId, resource));
		}

		public LoadResult recursiveLoad(Identifier id) {
			if (findStack.contains(id)) {
				// Make a copy of the find stack with the offending id added on top to show the full path.
				findStack.addLast(id);
				var copy = List.copyOf(findStack);
				findStack.removeLast();
				return new LoadResult.Failure(new LoadError.CircularDependency(id, copy));
			}
			findStack.addLast(id);

			LoadResult out = _find(id);

			findStack.removeLast();
			return out;
		}

		private LoadResult _find(Identifier id) {
			// Can't use computeIfAbsent because mutual recursion causes ConcurrentModificationExceptions
			var out = results.get(id);
			if (out == null) {
				out = load(id);
				results.put(id, out);
			}
			return out;
		}

		private LoadResult load(Identifier id) {
			return manager.getResource(id.withPrefix(SHADER_DIR))
					.map(resource -> readResource(id, resource))
					.orElseGet(() -> new LoadResult.Failure(new LoadError.ResourceError(id)));
		}

		private LoadResult readResource(Identifier id, Resource resource) {
			try (InputStream stream = resource.open()) {
				String sourceString = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				return SourceFile.parse(this::recursiveLoad, id, sourceString);
			} catch (IOException e) {
				return new LoadResult.Failure(new LoadError.IOError(id, e));
			}
		}
	}
}
