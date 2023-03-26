package com.jozufozu.flywheel.core.source;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.util.ResourceUtil;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The main object for loading and parsing source files.
 */
public class ShaderSources {
	public static final String SHADER_DIR = "flywheel/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");

	private final Map<ResourceLocation, SourceFile> cache = new HashMap<>();

	/**
	 * Tracks where we are in the mutual recursion to detect circular imports.
	 */
	private final Deque<ResourceLocation> findStack = new ArrayDeque<>();

	private final ResourceManager manager;
	private final ErrorReporter errorReporter;

	public ShaderSources(ErrorReporter errorReporter, ResourceManager manager) {
		this.errorReporter = errorReporter;
		this.manager = manager;
	}

	@Nonnull
	public SourceFile find(ResourceLocation location) {
		pushFindStack(location);
		// Can't use computeIfAbsent because mutual recursion causes ConcurrentModificationExceptions
		var out = cache.get(location);
		if (out == null) {
			out = load(location);
			cache.put(location, out);
		}
		popFindStack();
		return out;
	}

	@Nonnull
	private SourceFile load(ResourceLocation loc) {
		try {
			var resource = manager.getResource(ResourceUtil.prefixed(SHADER_DIR, loc));

			var sourceString = StringUtil.readToString(resource.getInputStream());

			return new SourceFile(this, loc, sourceString);
		} catch (IOException ioException) {
			throw new ShaderLoadingException("Could not load shader " + loc, ioException);
		}
	}

	private void generateRecursiveImportException(ResourceLocation location) {
		findStack.add(location);
		String path = findStack.stream()
				.dropWhile(l -> !l.equals(location))
				.map(ResourceLocation::toString)
				.collect(Collectors.joining(" -> "));
		findStack.clear();
		throw new ShaderLoadingException("recursive import: " + path);
	}

	private void pushFindStack(ResourceLocation location) {
		if (findStack.contains(location)) {
			generateRecursiveImportException(location);
		}
		findStack.add(location);
	}

	private void popFindStack() {
		findStack.pop();
	}
}
