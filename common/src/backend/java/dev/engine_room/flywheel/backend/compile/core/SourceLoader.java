package dev.engine_room.flywheel.backend.compile.core;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceFile;
import net.minecraft.resources.ResourceLocation;

public class SourceLoader {
	private final ShaderSources sources;
	private final CompilerStats stats;

	public SourceLoader(ShaderSources sources, CompilerStats stats) {
		this.sources = sources;
		this.stats = stats;
	}

	@Nullable
	public SourceFile find(ResourceLocation location) {
		var out = sources.find(location);
		stats.loadResult(out);
		return out.unwrap();
	}
}
