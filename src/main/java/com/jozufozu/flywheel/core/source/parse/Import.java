package com.jozufozu.flywheel.core.source.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.span.Span;

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
