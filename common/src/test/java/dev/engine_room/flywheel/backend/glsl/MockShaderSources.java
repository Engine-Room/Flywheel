package dev.engine_room.flywheel.backend.glsl;

import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;

public class MockShaderSources {
	private final Map<Identifier, String> sources = new HashMap<>();
	private final Map<Identifier, LoadResult> cache = new HashMap<>();
	private final Deque<Identifier> findStack = new ArrayDeque<>();


	public MockShaderSources() {

	}

	public void add(Identifier loc, String source) {
		sources.put(loc, source);
	}

	public LoadResult find(Identifier location) {
		if (findStack.contains(location)) {
			// Make a copy of the find stack with the offending location added on top to show the full path.
			findStack.addLast(location);
			var copy = List.copyOf(findStack);
			findStack.removeLast();
			return new LoadResult.Failure(new LoadError.CircularDependency(location, copy));
		}
		findStack.addLast(location);

		LoadResult out = load(location);

		findStack.removeLast();
		return out;
	}

	private LoadResult load(Identifier loc) {
		var out = cache.get(loc);
		if (out != null) {
			return out;
		}

		var loadResult = _load(loc);

		cache.put(loc, loadResult);

		return loadResult;
	}

	private LoadResult _load(Identifier loc) {
		var maybeFound = sources.get(loc);
		if (maybeFound == null) {
			return new LoadResult.Failure(new LoadError.IOError(loc, new FileNotFoundException(loc.toString())));
		}
		return SourceFile.parse(this::find, loc, maybeFound);
	}
}
