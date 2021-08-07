package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

import net.minecraft.util.ResourceLocation;

public class FileResolution {

	private final List<Span> foundSpans = new ArrayList<>();
	private final ResourceLocation fileLoc;
	private SourceFile file;


	public FileResolution(ResourceLocation fileLoc) {
		this.fileLoc = fileLoc;
	}

	public ResourceLocation getFileLoc() {
		return fileLoc;
	}

	@Nullable
	public SourceFile getFile() {
		return file;
	}

	public void addSpan(Span span) {
		foundSpans.add(span);
	}

	public void resolve(ShaderSources sources) {

		try {
			file = sources.source(fileLoc);
		} catch (RuntimeException error) {
			ErrorReporter.generateSpanError(foundSpans.get(0), "could not find source");
		}
	}
}
