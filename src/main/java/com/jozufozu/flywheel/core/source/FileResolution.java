package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.span.Span;

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
	 * Extra info about where this resolution is required. Includes ProgramSpecs and shader Spans.
	 */
	private final List<Consumer<ErrorBuilder>> extraCrashInfoProviders = new ArrayList<>();
	private final ResourceLocation fileLoc;
	private SourceFile file;

	FileResolution(ResourceLocation fileLoc) {
		this.fileLoc = fileLoc;
	}

	public ResourceLocation getFileLoc() {
		return fileLoc;
	}

	/**
	 * Non-null if this file is resolved because there would have been a crash otherwise.
	 * @return The file that this resolution resolves to.
	 */
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
		extraCrashInfoProviders.add(builder -> builder.pointAtFile(span.getSourceFile())
				.pointAt(span, 1));
		return this;
	}

	public void addSpec(ResourceLocation name) {
		extraCrashInfoProviders.add(builder -> builder.extra("needed by spec: " + name + ".json"));
	}

	/**
	 * Check to see if this file actually resolves to something.
	 *
	 * <p>
	 *     Called after all files are loaded. If we can't find the file here, it doesn't exist.
	 * </p>
	 *
	 * @return True if this file is resolved.
	 */
	boolean resolve(SourceFinder sources) {
		file = sources.findSource(fileLoc);

		if (file == null) {
			ErrorBuilder builder = ErrorBuilder.error(String.format("could not find source for file %s", fileLoc));
			for (Consumer<ErrorBuilder> consumer : extraCrashInfoProviders) {
				consumer.accept(builder);
			}
			Backend.LOGGER.error(builder.build());

			return false;
		}

		// Let the GC do its thing
		extraCrashInfoProviders.clear();
		return true;
	}

	void invalidate() {
		extraCrashInfoProviders.clear();
		file = null;
	}
}
