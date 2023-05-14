package com.jozufozu.flywheel.glsl;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class MockShaderSources extends ShaderSources {
	private final Map<ResourceLocation, String> sources = new HashMap<>();

	public MockShaderSources() {
		super(ResourceManager.Empty.INSTANCE);
	}

	public void add(ResourceLocation loc, String source) {
		sources.put(loc, source);
	}

	@NotNull
	@Override
	protected LoadResult load(ResourceLocation loc) {
		var maybeFound = sources.get(loc);
		if (maybeFound == null) {
			return new LoadResult.Failure(new LoadError.IOError(loc, new FileNotFoundException(loc.toString())));
		}
		return SourceFile.parse(this, loc, maybeFound);
	}

	public LoadResult assertLoaded(ResourceLocation loc) {
		Assertions.assertTrue(cache.containsKey(loc), "Expected " + loc + " to be cached");
		return cache.get(loc);
	}
}
