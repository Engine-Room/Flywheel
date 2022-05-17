package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
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

	private static final Map<ResourceLocation, FileResolution> ALL = new HashMap<>();
	private static boolean tooLate = false;

	/**
	 * Extra info about where this resolution is required. Includes shader Spans.
	 */
	private final List<Span> neededAt = new ArrayList<>();
	private final List<BiConsumer<ErrorReporter, SourceFile>> checks = new ArrayList<>();

	private final ResourceLocation fileLoc;

	private SourceFile file;

	FileResolution(ResourceLocation fileLoc) {
		this.fileLoc = fileLoc;
	}

	public static FileResolution get(ResourceLocation file) {
		if (!tooLate) {
			return ALL.computeIfAbsent(file, FileResolution::new);
		} else {
			// Lock the map after resolution has run.
			FileResolution fileResolution = ALL.get(file);

			// ...so crash immediately if the file isn't found.
			if (fileResolution == null) {
				throw new ShaderLoadingException("could not find source for file: " + file);
			}

			return fileResolution;
		}
	}

	/**
	 * Try and resolve all referenced source files, printing errors if any aren't found.
	 */
	public static void run(ErrorReporter errorReporter, SourceFinder sources) {
		for (FileResolution resolution : ALL.values()) {
			resolution.resolveAndCheck(errorReporter, sources);
		}

		tooLate = true;
	}

	private void resolveAndCheck(ErrorReporter errorReporter, SourceFinder sources) {
		file = sources.findSource(fileLoc);

		if (file == null) {
			ErrorBuilder builder = errorReporter.error(String.format("could not find source for file %s", fileLoc));
			for (Span location : neededAt) {
				builder.pointAtFile(location.getSourceFile())
						.pointAt(location, 1);
			}
		} else {
			runChecks(errorReporter);
		}

		// Let the GC do its thing
		neededAt.clear();
	}

	private void runChecks(ErrorReporter errorReporter) {
		for (var check : checks) {
			check.accept(errorReporter, file);
		}
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
		neededAt.add(span);
		return this;
	}

	public FileResolution validateWith(BiConsumer<ErrorReporter, SourceFile> check) {
		checks.add(check);
		return this;
	}

	@Override
	public String toString() {
		return "FileResolution[" + fileLoc + "]";
	}
}
