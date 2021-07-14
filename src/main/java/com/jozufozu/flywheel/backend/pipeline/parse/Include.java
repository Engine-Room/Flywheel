package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.Optional;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

import net.minecraft.util.ResourceLocation;

public class Include extends AbstractShaderElement {

	private final ShaderSources sources;
	private Span file;

	private ResourceLocation fileLoc;
	private SourceFile resolution;


	public Include(ShaderSources sources, Span self, Span file) {
		super(self);
		this.sources = sources;
		this.file = file;

		try {
			fileLoc = new ResourceLocation(file.get());
		} catch (RuntimeException error) {
			ErrorReporter.generateSpanError(file, "malformed source name");
		}
	}

	public boolean isResolved() {
		return resolution != null;
	}

	@Nullable
	public SourceFile getTarget() {
		return resolution;
	}

	public ResourceLocation getFile() {
		return fileLoc;
	}

	public void resolve() {

		if (fileLoc == null) return;

		try {
			resolution = sources.source(fileLoc);
		} catch (RuntimeException error) {
			ErrorReporter.generateSpanError(file, "could not find source");
		}
	}
}
