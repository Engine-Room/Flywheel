package com.jozufozu.flywheel.core.source.parse;

import java.util.Optional;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.span.Span;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public class Import extends AbstractShaderElement {

	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*use\\s+\"(.*)\"", Pattern.MULTILINE);

	public final FileResolution resolution;

	protected Import(Span self, FileResolution resolution, Span file) {
		super(self);
		this.resolution = resolution.addSpan(file);
	}

	@Nullable
	public static Import create(ErrorReporter errorReporter, Span self, Span file) {
		ResourceLocation fileLocation;
		try {
			fileLocation = new ResourceLocation(file.get());
		} catch (ResourceLocationException e) {
			errorReporter.generateSpanError(file, "malformed source location");
			return null;
		}

		return new Import(self, FileResolution.weak(fileLocation), file);
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
