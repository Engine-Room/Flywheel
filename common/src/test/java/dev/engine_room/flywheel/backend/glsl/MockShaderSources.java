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

	public LoadResult find(Identifier id) {
		if (findStack.contains(id)) {
			// Make a copy of the find stack with the offending id added on top to show the full path.
			findStack.addLast(id);
			var copy = List.copyOf(findStack);
			findStack.removeLast();
			return new LoadResult.Failure(new LoadError.CircularDependency(id, copy));
		}
		findStack.addLast(id);

		LoadResult out = load(id);

		findStack.removeLast();
		return out;
	}

	private LoadResult load(Identifier id) {
		var out = cache.get(id);
		if (out != null) {
			return out;
		}

		var loadResult = _load(id);

		cache.put(id, loadResult);

		return loadResult;
	}

	private LoadResult _load(Identifier id) {
		var maybeFound = sources.get(id);
		if (maybeFound == null) {
			return new LoadResult.Failure(new LoadError.IOError(id, new FileNotFoundException(id.toString())));
		}
		return SourceFile.parse(this::find, id, maybeFound);
	}
}
