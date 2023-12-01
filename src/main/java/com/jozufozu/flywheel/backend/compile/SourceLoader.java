package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.compile.core.CompilerStats;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceFile;

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
