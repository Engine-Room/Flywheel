package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.FileResolution;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

import net.minecraft.util.ResourceLocation;

public class Import extends AbstractShaderElement {

	public static final List<Import> IMPORTS = new ArrayList<>();

	private final Span file;

	private final FileResolution resolution;
	private final ResourceLocation fileLoc;

	public Import(ShaderSources parent, Span self, Span file) {
		super(self);
		this.file = file;

		fileLoc = toRL(file);
		resolution = parent.resolveFile(fileLoc);
		resolution.addSpan(file);

		IMPORTS.add(this);
	}

	private ResourceLocation toRL(Span file) {
		try {
			return new ResourceLocation(file.get());
		} catch (RuntimeException error) {
			ErrorReporter.generateSpanError(file, "malformed source name");
		}

		return new ResourceLocation("");
	}

	@Nullable
	public SourceFile getFile() {
		return resolution.getFile();
	}

	public ResourceLocation getFileLoc() {
		return resolution.getFileLoc();
	}
}
