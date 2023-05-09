package com.jozufozu.flywheel.glsl;

import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

public sealed interface LoadResult {
	static LoadResult success(SourceFile sourceFile) {
		return new Success(sourceFile);
	}

	@Nullable SourceFile unwrap();

	record Success(SourceFile source) implements LoadResult {
		@Override
		@NotNull
		public SourceFile unwrap() {
			return source;
		}
	}

	record IOError(ResourceLocation location, IOException exception) implements LoadResult {
		@Override
		public SourceFile unwrap() {
			return null;
		}
	}

	record IncludeError(ResourceLocation location, List<LoadResult> innerFailures) implements LoadResult {
		@Override
		public SourceFile unwrap() {
			return null;
		}
	}
}
