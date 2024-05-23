package dev.engine_room.flywheel.backend.glsl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface LoadResult {
	@Nullable
	default SourceFile unwrap() {
		return null;
	}

	record Success(SourceFile source) implements LoadResult {
		@Override
		@NotNull
		public SourceFile unwrap() {
			return source;
		}
	}

	record Failure(LoadError error) implements LoadResult {
	}
}
