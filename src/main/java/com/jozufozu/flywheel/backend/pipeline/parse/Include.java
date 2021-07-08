package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.Optional;

import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

import net.minecraft.util.ResourceLocation;

public class Include extends AbstractShaderElement {

	private final ShaderSources sources;
	private Span file;

	private SourceFile resolution;

	public Include(ShaderSources sources, Span self, Span file) {
		super(self);
		this.sources = sources;
		this.file = file;
	}

	public boolean isResolved() {
		return resolution != null;
	}

	public Optional<SourceFile> getTarget() {
		return Optional.ofNullable(resolution);
	}

	public ResourceLocation getFile() {
		return new ResourceLocation(file.get());
	}

	@Override
	public void checkErrors(ErrorReporter e) {

		String name = file.get();

		try {
			ResourceLocation loc = new ResourceLocation(name);
			resolution = sources.source(loc);
		} catch (RuntimeException error) {

		}

	}
}
