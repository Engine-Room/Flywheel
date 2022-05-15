package com.jozufozu.flywheel.core.source.parse;

import java.util.Optional;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.span.Span;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public class Import extends AbstractShaderElement {

	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*use\\s+\"(.*)\"", Pattern.MULTILINE);

	private final FileResolution resolution;

	protected Import(Span self, FileResolution resolution, Span file) {
		super(self);
		this.resolution = resolution.addSpan(file);
	}

	@Nullable
	public static Import create(Resolver resolver, Span self, Span file) {
		ResourceLocation fileLocation;
		try {
			fileLocation = new ResourceLocation(file.get());
		} catch (ResourceLocationException e) {
			ErrorReporter.generateSpanError(file, "malformed source location");
			return null;
		}

		return new Import(self, resolver.get(fileLocation), file);
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
