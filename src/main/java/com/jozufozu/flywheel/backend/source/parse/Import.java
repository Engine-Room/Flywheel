package com.jozufozu.flywheel.backend.source.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.Resolver;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.backend.source.error.ErrorReporter;
import com.jozufozu.flywheel.backend.source.span.Span;

import net.minecraft.resources.ResourceLocation;

public class Import extends AbstractShaderElement {

	public static final List<Import> IMPORTS = new ArrayList<>();

	private final Span file;

	private final FileResolution resolution;

	public Import(Resolver resolver, Span self, Span file) {
		super(self);
		this.file = file;

		resolution = resolver.get(toRL(file))
				.addSpan(file);

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

	public FileResolution getResolution() {
		return resolution;
	}

	public Optional<SourceFile> getOptional() {
		return Optional.ofNullable(resolution.getFile());
	}

	@Nullable
	public SourceFile getFile() {
		return resolution.getFile();
	}

	public ResourceLocation getFileLoc() {
		return resolution.getFileLoc();
	}
}
