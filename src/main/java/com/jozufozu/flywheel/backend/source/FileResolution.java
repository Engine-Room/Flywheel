package com.jozufozu.flywheel.backend.source;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.source.error.ErrorBuilder;
import com.jozufozu.flywheel.backend.source.span.Span;

import net.minecraft.resources.ResourceLocation;

/**
 * A reference to a source file that might not be loaded when the owning object is created.
 *
 * <p>
 *     FileResolutions are used primarily while parsing import statements. {@link FileResolution#file} is initially
 *     null, but will be populated later on, after <em>all</em> SourceFiles are loaded (assuming
 *     {@link FileResolution#fileLoc} references an actual file).
 * </p>
 */
public class FileResolution {

	/**
	 * Spans that have references that resolved to this.
	 */
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

	/**
	 * Store the given span so this resolution can know all the places that reference the file.
	 *
	 * <p>
	 *     Used for error reporting.
	 * </p>
	 * @param span A span where this file is referenced.
	 */
	public FileResolution addSpan(Span span) {
		foundSpans.add(span);
		return this;
	}

	/**
	 * Check to see if this file actually resolves to something.
	 *
	 * <p>
	 *     Called after all files are loaded. If we can't find the file here, it doesn't exist.
	 * </p>
	 */
	void resolve(ISourceHolder sources) {

		try {
			file = sources.findSource(fileLoc);
		} catch (RuntimeException error) {
			ErrorBuilder builder = ErrorBuilder.error(String.format("could not find source for file %s", fileLoc));
			// print the location of all places where this file was referenced
			for (Span span : foundSpans) {
				builder.pointAtFile(span.getSourceFile())
						.pointAt(span, 2);
			}
			Backend.log.error(builder.build());
		}
	}

	void invalidate() {
		foundSpans.clear();
		file = null;
	}
}
